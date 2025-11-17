package io.dungeons

import io.dungeons.Die.Companion.D20
import io.dungeons.RangeClassification.*
import io.dungeons.combat.ProvidesGridPosition
import io.dungeons.world.GridPosition

interface Attacker {
    val id: Id<CoreEntity>
    val attackSource: AttackSource
    val stats: StatBlock
    fun applyAttackModifiers(): Int
    fun isCriticalHit(hitRoll: DieRoll): Boolean
}

interface Attackable {
    val id: Id<CoreEntity>
    val armourClass: Int
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

internal fun attack(
    attacker: Attacker,
    opponent: Attackable,
    providesGridPosition: ProvidesGridPosition,
    rollModifier: RollModifier = RollModifier.NORMAL): AttackOutcome {
    val hitRollD20 = rollModifier.let {
        val attackerPos : GridPosition = providesGridPosition.getGridPosition(attacker.id) ?: error("No position found for entity $attacker")
        val opponentPos = providesGridPosition.getGridPosition(opponent.id) ?: error("No position found for entity $opponent")
        val distance = attackerPos.chebyshevDistance(opponentPos)
        when (attacker.attackSource.isTargetInRange(distance.toFeet())) {
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
