package io.dungeons.domain.combat

import io.dungeons.domain.Creature
import io.dungeons.domain.DieRoll
import io.dungeons.domain.board.BoardPosition
import io.dungeons.domain.core.Id

enum class FactionStance {
    Friendly,
    Neutral,
    Hostile,
}

data class Faction(val id: Id<Faction> = Id.Companion.generate(), val name: String)

data class FactionRelationship(val first: Faction, val second: Faction, val stance: FactionStance) {
    internal fun toLookupKey(): FactionRelationsLookupKey = setOf(first.id, second.id)
}

private typealias FactionRelationsLookupKey = Set<Id<Faction>>

class FactionRelations private constructor(private val relationships: Map<FactionRelationsLookupKey, FactionStance>) {
    fun queryStance(factionA: Faction, factionB: Faction): FactionStance {
        if (factionA.id == factionB.id) {
            return FactionStance.Friendly
        }
        val key = setOf(factionA.id, factionB.id)
        return relationships[key] ?: DEFAULT_STANCE
    }

    companion object {
        private val DEFAULT_STANCE = FactionStance.Hostile
    }

    class Builder {
        private val relationships: MutableMap<FactionRelationsLookupKey, FactionStance> = mutableMapOf()

        fun add(relationshipToAdd: FactionRelationship): Builder {
            val key = relationshipToAdd.toLookupKey()
            if (key in relationships) {
                throw IllegalArgumentException("Relationship $relationshipToAdd already exists.")
            }
            if (relationshipToAdd.stance != FactionRelations.DEFAULT_STANCE) {
                relationships[key] = relationshipToAdd.stance
            }
            return this
        }

        fun build(): FactionRelations = FactionRelations(relationships.toMap())
    }
}

data class Turn(
    val round: Int,
    val movementAvailable: Boolean = true,
    val actionAvailable: Boolean = true,
    val bonusActionAvailable: Boolean = true,
    val reactionAvailable: Boolean = true,
) {
    val hasOptionsForTurnLeft: Boolean
        get() = movementAvailable ||
            actionAvailable ||
            bonusActionAvailable

    @Suppress("DataClassContainsFunctions")
    fun useMovement(): Turn {
        require(movementAvailable) { "Movement already used this turn." }
        return copy(movementAvailable = false)
    }

    @Suppress("DataClassContainsFunctions")
    fun useAction(): Turn {
        require(actionAvailable) { "Action already used this turn." }
        return copy(actionAvailable = false)
    }

    @Suppress("DataClassContainsFunctions")
    fun useBonusAction(): Turn {
        require(bonusActionAvailable) { "Bonus action already used this turn." }
        return copy(bonusActionAvailable = false)
    }

    @Suppress("DataClassContainsFunctions")
    fun useReaction(): Turn {
        require(reactionAvailable) { "Reaction already used this turn." }
        return copy(reactionAvailable = false)
    }
}

interface TurnActor {
    fun handleTurn(combatant: Combatant, turn: Turn, combatScenario: CombatScenario): CombatCommand?
}

internal class NoopTurnActor : TurnActor {
    override fun handleTurn(combatant: Combatant, turn: Turn, combatScenario: CombatScenario): CombatCommand? = null
}

data class Combatant(val creature: Creature, val faction: Faction, val actor: TurnActor) {
    val id: Id<Creature>
        get() = creature.id

    val initiative: DieRoll by lazy {
        creature.rollInitiative()
    }

    val hitPoints: Int
        get() = creature.hitPoints
}

interface ProvidesBoardPosition {
    fun getBoardPosition(creatureId: Id<Creature>): BoardPosition?
}
