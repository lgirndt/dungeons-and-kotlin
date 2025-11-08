package org.example

import org.example.Die.Companion.D20
import org.example.RangeClassification.OutOfRange
import org.example.RangeClassification.WithinLongRange
import org.example.RangeClassification.WithinNormalRange

interface Attackable {
    val armourClass: Int
    val position: Coordinate
    fun receiveDamage(amount: Int, damageType: DamageType): Int
}

data class AttackOutcome(
    val hasBeenHit: Boolean,
    val damageDealt: Int,
    val hitRoll: Int = 0,
) {
    companion object {
        val MISS = AttackOutcome(false, 0)
    }
}

typealias WeaponProficiency = (Weapon) -> Boolean

// TODO category is also coming from weapon, do we need both?
internal object WeaponProficiencies {
    val all: WeaponProficiency = { _ -> true }
    val simple: WeaponProficiency = { weapon -> weapon.category == WeaponCategory.Simple }
    val none: WeaponProficiency = { _ -> false }
}


data class Character(
    val name: String,
    val characterClass: CharacterClass,
    val stats: StatBlock,
    val level: Int = 1,
    val damageModifiers: DamageModifiers = DamageModifiers.NONE,
    var currentWeapon: Weapon? = null,
    var hitPoints: Int,
    val armour: (StatBlock) -> Int,
    override val position: Coordinate = Coordinate(0, 0),
) : Attackable {
    val proficiencyBonus: Int get() = 1 + (level - 1) / 4

    override val armourClass: Int get() = armour(stats)

    fun equip(weapon: Weapon) {
        this.currentWeapon = weapon
    }

    fun attack(opponent: Attackable, rollModifier: RollModifier = RollModifier.NORMAL): AttackOutcome {
        // to hit
        val currentWeapon = this.currentWeapon ?: return AttackOutcome.MISS

        val hitRollD20 = rollModifier.let {
            val distance = position.distance(opponent.position)
            when (currentWeapon.isTargetInRange(distance)) {
                OutOfRange -> return AttackOutcome.MISS
                WithinNormalRange -> it
                WithinLongRange -> it.giveDisadvantage()
            }
        }.roll(D20)

        val hitRoll = applyAttackModifiers(hitRollD20, currentWeapon)

        return if (hitRoll >= opponent.armourClass) {
            // damage
            val damage = currentWeapon.dealDamage(stats, isCriticalHit(hitRollD20))
            val receivedDamage = opponent.receiveDamage(damage, currentWeapon.damageType)

            AttackOutcome(true, receivedDamage, hitRoll)
        } else {
            AttackOutcome(false, 0, hitRoll)
        }
    }

    private fun applyAttackModifiers(hitRollD20: DieRoll, currentWeapon: Weapon): Int {
        val proficiencyModifier = if (isProficientWith(currentWeapon)) proficiencyBonus else 0
        val modifier = currentWeapon.receiveModifier(stats)

        val hitRoll = hitRollD20.value + modifier + proficiencyModifier
        return hitRoll
    }

    override fun receiveDamage(amount: Int, damageType: DamageType): Int {
        val adjustedAmount = when (damageType) {
            in damageModifiers.immunities -> 0
            in damageModifiers.resistances -> amount / 2
            in damageModifiers.vulnerabilities -> amount * 2
            else -> amount
        }

        hitPoints = (hitPoints - adjustedAmount).coerceAtLeast(0)

        return adjustedAmount
    }

    private fun isProficientWith(weapon: Weapon): Boolean {
        return characterClass.isProficientWith(weapon)
    }

    private fun isCriticalHit(hitRoll: DieRoll): Boolean {
        return hitRoll.value == 20
    }

}
