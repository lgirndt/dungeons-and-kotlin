package io.dungeons.combat

import io.dungeons.Creature
import io.dungeons.DieRoll
import io.dungeons.core.Id
import io.dungeons.board.BoardPosition
import io.dungeons.world.Square
import io.dungeons.world.isInRange

enum class FactionStance {
    Friendly,
    Neutral,
    Hostile
}

data class Faction(
    val id: Id<Faction> = Id.Companion.generate(),
    val name: String,
)

data class FactionRelationship(
    val first: Faction,
    val second: Faction,
    val stance: FactionStance
) {
    internal fun toLookupKey(): FactionRelationsLookupKey {
        return setOf(first.id, second.id)
    }
}

private typealias FactionRelationsLookupKey = Set<Id<Faction>>

class FactionRelations private constructor(
    private val relationships: Map<FactionRelationsLookupKey, FactionStance>
) {
    companion object {
        private val DEFAULT_STANCE = FactionStance.Hostile
    }

    fun queryStance(factionA: Faction, factionB: Faction): FactionStance {
        if (factionA.id == factionB.id) {
            return FactionStance.Friendly
        }
        val key = setOf(factionA.id, factionB.id)
        return relationships[key] ?: DEFAULT_STANCE
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

        fun build(): FactionRelations {
            return FactionRelations(relationships.toMap())
        }
    }
}

data class Turn(
    val round: Int,
    val movementAvailable: Boolean = true,
    val actionAvailable: Boolean = true,
    val bonusActionAvailable: Boolean = true,
    val reactionAvailable: Boolean = true
) {

    val hasOptionsForTurnLeft: Boolean
        get() = movementAvailable
                || actionAvailable
                || bonusActionAvailable

    fun useMovement(): Turn {
        require(movementAvailable) { "Movement already used this turn." }
        return copy(movementAvailable = false)
    }

    fun useAction(): Turn {
        require(actionAvailable) { "Action already used this turn." }
        return copy(actionAvailable = false)
    }

    fun useBonusAction(): Turn {
        require(bonusActionAvailable) { "Bonus action already used this turn." }
        return copy(bonusActionAvailable = false)
    }

    fun useReaction(): Turn {
        require(reactionAvailable) { "Reaction already used this turn." }
        return copy(reactionAvailable = false)
    }

}

interface TurnActor {
    fun handleTurn(combatant: Combatant, turn: Turn, combatScenario: CombatScenario): CombatCommand?
}

internal class NoopTurnActor : TurnActor {
    override fun handleTurn(
        combatant: Combatant,
        turn: Turn,
        combatScenario: CombatScenario
    ): CombatCommand? {
        return null
    }
}

data class Combatant(
    val creature: Creature,
    val faction: Faction,
    val actor: TurnActor,
) {
    val id: Id<Creature>
        get() = creature.id

    val initiative: DieRoll by lazy {
        creature.rollInitiative()
    }

    val hitPoints: Int
        get() = creature.hitPoints
}

class CombatantsStore(
    combatants: Collection<Combatant>,
    nonHostileFactionRelationships: List<FactionRelationship> = emptyList(),
) {
    val combatants: Map<Id<Creature>, Combatant> = combatants.associateBy { it.id }

    val factionRelations: FactionRelations = nonHostileFactionRelationships
        .onEach {
            require(it.stance == FactionStance.Hostile) {
                "CombatantsStore can only accept non-hostile relationships."
            }
        }
        .fold(FactionRelations.Builder()) { builder, relationship ->
            builder.add(relationship)
        }.build()

    fun findOrNull(id: Id<Creature>): Combatant? {
        return combatants[id]
    }

    fun findAllWithStance(towards: Id<Creature>, stance: FactionStance): List<Combatant> {
        val combatant = combatants[towards] ?: return emptyList()
        val targetFaction = combatant.faction
        return combatants.values.filter {
            val relationStance = factionRelations.queryStance(targetFaction, it.faction)
            relationStance == stance
        }
    }

    fun listAll(): List<Combatant> {
        return combatants.values.toList()
    }
}

interface ProvidesBoardPosition {
    fun getBoardPosition(creatureId: Id<Creature>): BoardPosition?
}

interface CombatScenario : ProvidesBoardPosition {
    fun listVisibleCombatants(observer: Id<Creature>): List<Combatant>
    fun isVisibleTo(observer: Id<Creature>, target: Id<Creature>): Boolean
    fun listCombatantsInRange(observer: Id<Creature>, rangeInSquares: Square): List<Combatant>
    override fun getBoardPosition(creatureId: Id<Creature>): BoardPosition?
}

class SimpleCombatScenario(
    private val combatantsStore: CombatantsStore
) : CombatScenario {

    override fun listVisibleCombatants(observer: Id<Creature>): List<Combatant> {
        return combatantsStore.listAll().filter { it.id != observer }
    }

    override fun isVisibleTo(
        observer: Id<Creature>,
        target: Id<Creature>
    ): Boolean = true


    override fun listCombatantsInRange(
        observer: Id<Creature>,
        rangeInSquares: Square
    ): List<Combatant> {
        val position = getBoardPosition(observer)
            ?: error("No grid position found for creature $observer")

        return combatantsStore.listAll()
            .filter { it.id != observer }
            .filter {
                getBoardPosition(it.creature.id)?.let { targetPos ->
                    isInRange(position, targetPos, rangeInSquares)
                } ?: true
            }
    }

    override fun getBoardPosition(creatureId: Id<Creature>): BoardPosition? {
        TODO("Not yet implemented")
    }

}