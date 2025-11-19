package io.dungeons

import io.dungeons.Die.Companion.D20
import io.dungeons.combat.ProvidesGridPosition
import io.dungeons.core.Id


data class CoreEntityData(
    val name: String,
    val stats: StatBlock,
    var hitPoints: Int,
    val armourClass: Int,
    val damageModifiers: DamageModifiers,
)

abstract class CoreEntity(
    override val id: Id<CoreEntity>,
    protected val core: CoreEntityData,
) : Attackable {

    val name: String
        get() = core.name

    val stats: StatBlock
        get() = core.stats

    override var hitPoints: Int
        get() = core.hitPoints
        set(value) {
            core.hitPoints = value
        }

    override val damageModifiers: DamageModifiers
        get() = core.damageModifiers

    override val armourClass: Int
        get() = core.armourClass

    abstract val weapon: Weapon

    protected abstract val attackModifier: Int
    protected open fun isCriticalHit(hitRoll: DieRoll): Boolean {
        return hitRoll.value == 20
    }

    internal fun asPhysicalAttacker(): Attacker {
        val entity = this
        return object : Attacker {
            override val id: Id<CoreEntity>
                get() = entity.id
            override val attackSource = entity.weapon
            override val stats: StatBlock
                get() = core.stats

            override fun applyAttackModifiers(): Int = entity.attackModifier
            override fun isCriticalHit(hitRoll: DieRoll): Boolean =
                entity.isCriticalHit(hitRoll)
        }
    }

    fun attack(
        opponent: Attackable,
        providesGridPosition: ProvidesGridPosition,
        rollModifier: RollModifier = RollModifier.NORMAL): AttackOutcome {
        val attacker = asPhysicalAttacker()
        return attack(attacker, opponent, providesGridPosition, rollModifier)
    }

    fun rollAbilityCheck(
        ability: StatQuery,
        difficultyClass: Int,
        rollModifier: RollModifier = RollModifier.NORMAL): AbilityCheckResult {
        val roll = rollModifier.roll(D20)
        return AbilityCheckResult(roll.value + ability(stats).modifier >= difficultyClass)
    }

    fun rollInitiative(rollModifier: RollModifier = RollModifier.NORMAL): DieRoll {
        val roll = rollModifier.roll(D20)
        val dexMod = StatQueries.Dex(stats).modifier
        return DieRoll(D20, roll.value + dexMod)
    }
}