package io.dungeons.combat

import io.dungeons.*

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
    val entity: CoreEntity,
    val faction: Faction,
    val actor: TurnActor = NoopTurnActor()
) {
    val id: Id<CoreEntity>
        get() = entity.id

    val initiative: DieRoll by lazy {
        entity.rollInitiative()
    }

    val hitPoints: Int
        get() = entity.hitPoints
}

class CombatantsStore(
    combatants: Collection<Combatant>,
    nonHostileFactionRelationships: List<FactionRelationship> = emptyList(),
) {
    val combatants: Map<Id<CoreEntity>, Combatant> = combatants.associateBy { it.id }

    val factionRelations: FactionRelations = nonHostileFactionRelationships
        .onEach {
            require(it.stance == FactionStance.Hostile) {
                "CombatantsStore can only accept non-hostile relationships."
            }
        }
        .fold(FactionRelations.Builder()) { builder, relationship ->
            builder.add(relationship)
        }.build()

    fun findOrNull(id: Id<CoreEntity>): Combatant? {
        return combatants[id]
    }

    fun findAllWithStance(towards: Id<CoreEntity>, stance: FactionStance): List<Combatant> {
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

interface CombatScenario {
    fun listVisibleCombatants(observer: Id<CoreEntity>): List<Combatant>
    fun isVisibleTo(observer: Id<CoreEntity>, target: Id<CoreEntity>): Boolean
    fun listCombatantsInRange(observer: Id<CoreEntity>, rangeInFeet: Feet): List<Combatant>
}

class SimpleCombatScenario(
    private val combatantsStore: CombatantsStore
) : CombatScenario {

    override fun listVisibleCombatants(observer: Id<CoreEntity>): List<Combatant> {
        return combatantsStore.listAll().filter { it.id != observer }
    }

    override fun isVisibleTo(
        observer: Id<CoreEntity>,
        target: Id<CoreEntity>
    ): Boolean = true


    override fun listCombatantsInRange(
        observer: Id<CoreEntity>,
        rangeInFeet: Feet
    ): List<Combatant> {
        val entity = combatantsStore.findOrNull(observer)?.entity
            ?: return emptyList()

        return combatantsStore.listAll()
            .filter { it.id != observer }
            .filter { isInRange(entity.position, it.entity.position, rangeInFeet) }
    }

}