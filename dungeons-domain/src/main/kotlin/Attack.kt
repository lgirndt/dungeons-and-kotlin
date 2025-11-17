package io.dungeons

import io.dungeons.Die.Companion.D20
import io.dungeons.RangeClassification.*
import io.dungeons.world.Coordinate

interface Attacker {
    val attackSource: AttackSource
    val position: Coordinate
    val stats: StatBlock
    fun applyAttackModifiers(): Int
    fun isCriticalHit(hitRoll: DieRoll): Boolean
}

interface Attackable {
    val armourClass: Int
    val position: Coordinate
    val damageModifiers: DamageModifiers
    var hitPoints: Int
    fun receiveDamage(amount: Int, damageType: DamageType): Int {
        val adjustedAmount = when (damageType) {
            in damageModifiers.immunities -> 0
            in damageModifiers.resistances -> amount / 2
            in damageModifiers.vulnerabilities -> amount * 2
            else -> amount
        }

        hitPoints = (hitPoints - adjustedAmount).coerceAtLeast(0)

        return adjustedAmount
    }
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

internal fun attack(attacker: Attacker, opponent: Attackable, rollModifier: RollModifier = RollModifier.NORMAL): AttackOutcome {
    val hitRollD20 = rollModifier.let {
        val distance = attacker.position.distance(opponent.position)
        when (attacker.attackSource.isTargetInRange(distance)) {
            OutOfRange -> return AttackOutcome.MISS
            WithinNormalRange -> it
            WithinLongRange -> it.giveDisadvantage()
        }
    }.roll(D20)
    val isCrit = attacker.isCriticalHit(hitRollD20)

    val hitRoll = hitRollD20.value + attacker.applyAttackModifiers()

    return if (hitRoll >= opponent.armourClass) {
        // damage
        val damage = attacker.attackSource.dealDamage({ query : StatQuery -> query(attacker.stats)}, isCrit)
        val receivedDamage = opponent.receiveDamage(damage, attacker.attackSource.damageType)

        AttackOutcome(true, receivedDamage, hitRoll)
    } else {
        AttackOutcome(false, 0, hitRoll)
    }
}
