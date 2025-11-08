package org.example

import org.example.Die.Companion.D20
import org.example.RangeClassification.OutOfRange
import org.example.RangeClassification.WithinLongRange
import org.example.RangeClassification.WithinNormalRange

interface Attacker {
    val currentWeapon: Weapon?
    val position: Coordinate
    val stats: StatBlock
    fun applyAttackModifiers(weapon: Weapon): Int
    fun isCriticalHit(hitRoll: DieRoll): Boolean
}

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

fun attack(attacker: Attacker, opponent: Attackable, rollModifier: RollModifier = RollModifier.NORMAL): AttackOutcome {
    // to hit
    val currentWeapon = attacker.currentWeapon ?: return AttackOutcome.MISS

    val hitRollD20 = rollModifier.let {
        val distance = attacker.position.distance(opponent.position)
        when (currentWeapon.isTargetInRange(distance)) {
            OutOfRange -> return AttackOutcome.MISS
            WithinNormalRange -> it
            WithinLongRange -> it.giveDisadvantage()
        }
    }.roll(D20)
    val isCrit = attacker.isCriticalHit(hitRollD20)

    val hitRoll = hitRollD20.value + attacker.applyAttackModifiers(currentWeapon)

    return if (hitRoll >= opponent.armourClass) {
        // damage
        val damage = currentWeapon.dealDamage(attacker.stats, isCrit)
        val receivedDamage = opponent.receiveDamage(damage, currentWeapon.damageType)

        AttackOutcome(true, receivedDamage, hitRoll)
    } else {
        AttackOutcome(false, 0, hitRoll)
    }
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

    fun asAttacker(): Attacker {
        val parent = this
        return object: Attacker {
            override val currentWeapon: Weapon? = parent.currentWeapon
            override val position: Coordinate = parent.position
            override val stats: StatBlock = parent.stats
            override fun applyAttackModifiers(weapon: Weapon): Int = parent.applyAttackModifiers(weapon)
            override fun isCriticalHit(hitRoll: DieRoll): Boolean = parent.isCriticalHit(hitRoll)
        }
    }

    fun attack(opponent: Attackable, rollModifier: RollModifier = RollModifier.NORMAL): AttackOutcome {
        return attack(asAttacker(), opponent, rollModifier)
    }

    private fun applyAttackModifiers(weapon: Weapon): Int {
        val proficiencyModifier = if (isProficientWith(weapon)) proficiencyBonus else 0
        val modifier = weapon.receiveModifier(stats)

        val hitRoll = modifier + proficiencyModifier
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
