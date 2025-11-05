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
    companion object

    val proficiencyBonus: Int get() = 1 + (level - 1) / 4

    override val armourClass: Int get() = armour(stats)

    fun equip(weapon: Weapon) {
        this.currentWeapon = weapon
    }

    fun attack(opponent: Attackable): AttackOutcome {
        // to hit
        val currentWeapon = this.currentWeapon ?: return AttackOutcome.MISS
        val modifier = currentWeapon.receiveModifier(stats)

        val hitRollD20 = D20.roll()
        val proficiencyModifier = if (isProficientWith(currentWeapon)) proficiencyBonus else 0
        val hitRoll = hitRollD20 + modifier + proficiencyModifier

        if (hitRoll >= opponent.armourClass) {
            // damage
            val isCritical = hitRollD20 == 20
            val damage = currentWeapon.dealDamage(stats, isCritical)
            val receivedDamage = opponent.receiveDamage(damage, currentWeapon.damageType)

            return AttackOutcome(true, receivedDamage, hitRoll)
        }
        return AttackOutcome(false, 0, hitRoll)
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
        // TODO
        return true
    }

}
