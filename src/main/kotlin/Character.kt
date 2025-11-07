package org.example

import org.example.Die.Companion.D20

interface Attackable {
    val armourClass: Int
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

typealias WeaponProficiency = (WeaponCategory, Weapon) -> Boolean

internal object WeaponProficiencies {
    val all : WeaponProficiency = { _ , _ -> true }
    val simple : WeaponProficiency = { category, _ -> category == WeaponCategory.Simple }
    val none : WeaponProficiency = { _, _ -> false }
}

sealed class CharacterClass(
    private val hitDie: Die,
    private val weaponProficiency: WeaponProficiency = WeaponProficiencies.none
) {
    val name: String
        get() = this::class.simpleName!!

    fun isProficientWith(weapon: Weapon): Boolean = weaponProficiency(weapon.category, weapon)

    data object Fighter : CharacterClass(Die.D10, WeaponProficiencies.all)
    data object Cleric : CharacterClass(Die.D8, WeaponProficiencies.simple)
    data object Druid : CharacterClass(Die.D8, WeaponProficiencies.simple)
    data object Barbarian : CharacterClass(Die.D12, WeaponProficiencies.all)
    data object Paladin : CharacterClass(Die.D10)
    data object Ranger : CharacterClass(Die.D10)
    data object Rogue : CharacterClass(Die.D8)
    data object Warlock : CharacterClass(Die.D8)
    data object Monk : CharacterClass(Die.D8)
    data object Sorcerer : CharacterClass(Die.D6)
    data object Bard : CharacterClass(Die.D8, WeaponProficiencies.simple)
    data object Wizard : CharacterClass(Die.D8)
}

data class Character(
    val name: String,
    val characterClass: CharacterClass,
    val stats: StatBlock,
    val level: Int = 1,
    val damageModifiers: DamageModifiers = DamageModifiers.NONE,
    var currentWeapon: Weapon? = null,
    var hitPoints: Int,
    val armour: (StatBlock) -> Int
) : Attackable {
    val proficiencyBonus: Int get() = 1 + (level - 1) / 4

    override val armourClass: Int get() = armour(stats)

    fun equip(weapon: Weapon) {
        this.currentWeapon = weapon
    }

    fun attack(opponent: Attackable, rollModifier: RollModifier = RollModifier.NORMAL): AttackOutcome {
        // to hit
        val currentWeapon = this.currentWeapon ?: return AttackOutcome.MISS
        val modifier = currentWeapon.receiveModifier(stats)

        val hitRollD20 = rollModifier.roll(D20)
        val proficiencyModifier = if (isProficientWith(currentWeapon)) proficiencyBonus else 0
        val hitRoll = hitRollD20.value + modifier + proficiencyModifier

        return if (hitRoll >= opponent.armourClass) {
            // damage
            val damage = currentWeapon.dealDamage(stats, isCriticalHit(hitRollD20))
            val receivedDamage = opponent.receiveDamage(damage, currentWeapon.damageType)

            AttackOutcome(true, receivedDamage, hitRoll)
        } else {
            AttackOutcome(false, 0, hitRoll)
        }
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

    private fun isCriticalHit(hitRoll: DieRoll) : Boolean {
        return hitRoll.value == 20
    }

}
