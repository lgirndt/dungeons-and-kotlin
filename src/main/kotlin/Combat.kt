package org.example

import com.google.common.collect.Multimap

enum class FactionStance {
    Friendly,
    Neutral,
    Hostile
}

data class Faction (
    val id: Id<Faction> = Id.generate(),
    val name: String,
)

data class FactionRelationship (
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
            if ( relationshipToAdd.stance != FactionRelations.DEFAULT_STANCE) {
                relationships[key] = relationshipToAdd.stance
            }
            return this
        }

        fun build(): FactionRelations {
            return FactionRelations(relationships.toMap())
        }
    }
}

data class Combatant (
    val entity: CoreEntity,
    val faction: Faction
)

class CombatantsStore (
    combatantsByFaction: Multimap<Faction, CoreEntity>,
    nonHostileFactionRelationships: List<FactionRelationship> = emptyList(),
) {
    val combatants : Map<Id<CoreEntity>, Combatant> = combatantsByFaction.entries()
        .map { Combatant(faction = it.key, entity = it.value) }
        .associateBy { it.entity.id }

    val factionRelations: FactionRelations = nonHostileFactionRelationships
        .onEach {
            require (it.stance == FactionStance.Hostile) {
                "CombatantsStore can only accept non-hostile relationships."
            }
        }
        .fold(FactionRelations.Builder()) {
        builder, relationship ->
        builder.add(relationship)
    }.build()

    fun find(id: Id<CoreEntity>) : Combatant? {
        return combatants[id]
    }

    fun findAllWithStance(towards: Id<CoreEntity>, stance: FactionStance) : List<Combatant> {
        val combatant = combatants[towards] ?: return emptyList()
        val targetFaction = combatant.faction
        return combatants.values.filter {
            val relationStance = factionRelations.queryStance(targetFaction, it.faction)
            relationStance == stance
        }
    }
}