package org.example

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

class FactionRelations(
    private val relationships: MutableMap<FactionRelationsLookupKey, FactionStance> = mutableMapOf()
) {
    private val DEFAULT_STANCE = FactionStance.Hostile

    fun add(relationshipToAdd: FactionRelationship) {
        val key = relationshipToAdd.toLookupKey()
        if (key in relationships) {
            throw IllegalArgumentException("Relationship $relationshipToAdd already exists.")
        }
        if ( relationshipToAdd.stance != DEFAULT_STANCE) {
            relationships[key] = relationshipToAdd.stance
        }
    }

    fun queryStance(factionA: Faction, factionB: Faction): FactionStance {
        if (factionA.id == factionB.id) {
            return FactionStance.Friendly
        }
        val key = setOf(factionA.id, factionB.id)
        return relationships[key] ?: DEFAULT_STANCE
    }
}

data class Combatant  (
    val entity: CoreEntity,
    val faction: Faction)


