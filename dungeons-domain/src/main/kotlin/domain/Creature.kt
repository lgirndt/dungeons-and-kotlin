package io.dungeons.domain

import io.dungeons.domain.Die.Companion.D20
import io.dungeons.domain.combat.ProvidesBoardPosition
import io.dungeons.domain.core.Id

@Suppress("DataClassShouldBeImmutable") // we are still learning how to model the creature properly
data class CreatureData(
    val name: String,
    val stats: StatBlock,
    var hitPoints: Int,
    val armourClass: Int,
    val damageModifiers: DamageModifiers,
) {
    val maxHitPoints: Int = hitPoints
}

abstract class Creature(override val id: Id<Creature>, protected val core: CreatureData) : Attackable {
    val name: String
        get() = core.name

    val stats: StatBlock
        get() = core.stats

    override var hitPoints: Int
        get() = core.hitPoints
        set(value) {
            core.hitPoints = value
        }

    val maxHitPoints: Int
        get() = core.maxHitPoints

    override val damageModifiers: DamageModifiers
        get() = core.damageModifiers

    override val armourClass: Int
        get() = core.armourClass

    abstract val weapon: Weapon

    protected abstract val attackModifier: Int

    protected open fun isCriticalHit(hitRoll: DieRoll): Boolean = hitRoll.value == NATURAL_TWENTY

    internal fun asPhysicalAttacker(): Attacker {
        val creature = this
        return object : Attacker {
            override val id: Id<Creature>
                get() = creature.id
            override val attackSource = creature.weapon
            override val stats: StatBlock
                get() = core.stats

            override fun applyAttackModifiers(): Int = creature.attackModifier

            override fun isCriticalHit(hitRoll: DieRoll): Boolean = creature.isCriticalHit(hitRoll)
        }
    }

    fun attack(
        opponent: Attackable,
        providesBoardPosition: ProvidesBoardPosition,
        rollModifier: RollModifier = RollModifier.NORMAL,
    ): AttackOutcome {
        val attacker = asPhysicalAttacker()
        return attack(attacker, opponent, providesBoardPosition, rollModifier)
    }

    fun rollAbilityCheck(
        ability: StatQuery,
        difficultyClass: Int,
        rollModifier: RollModifier = RollModifier.NORMAL,
    ): AbilityCheckResult {
        val roll = rollModifier.roll(Die.D20)
        return AbilityCheckResult(roll.value + ability(stats).modifier >= difficultyClass)
    }

    fun rollInitiative(rollModifier: RollModifier = RollModifier.NORMAL): DieRoll {
        val roll = rollModifier.roll(Die.D20)
        val dexMod = StatQueries.Dex(stats).modifier
        return DieRoll(D20, roll.value + dexMod)
    }
}
