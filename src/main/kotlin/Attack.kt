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
