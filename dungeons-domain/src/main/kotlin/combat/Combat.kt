package io.dungeons.combat

import com.google.common.collect.Multimap
import io.dungeons.CoreEntity
import io.dungeons.DieRoll

enum class FactionStance {
    Friendly,
    Neutral,
    Hostile
}

data class Faction(
    val id: io.dungeons.Id<Faction> = _root_ide_package_.io.dungeons.Id.Companion.generate(),
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

private typealias FactionRelationsLookupKey = Set<io.dungeons.Id<Faction>>

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

data class Combatant(
    val entity: CoreEntity,
    val faction: Faction
) {
    val initiative: DieRoll by lazy {
        entity.rollInitiative()
    }
}

class CombatantsStore(
    combatantsByFaction: Multimap<Faction, io.dungeons.CoreEntity>,
    nonHostileFactionRelationships: List<FactionRelationship> = emptyList(),
) {
    val combatants: Map<io.dungeons.Id<io.dungeons.CoreEntity>, Combatant> = combatantsByFaction.entries()
        .map { Combatant(faction = it.key, entity = it.value) }
        .associateBy { it.entity.id }

    val factionRelations: FactionRelations = nonHostileFactionRelationships
        .onEach {
            require(it.stance == FactionStance.Hostile) {
                "CombatantsStore can only accept non-hostile relationships."
            }
        }
        .fold(FactionRelations.Builder()) { builder, relationship ->
            builder.add(relationship)
        }.build()

    fun findOrNull(id: io.dungeons.Id<io.dungeons.CoreEntity>): Combatant? {
        return combatants[id]
    }

    fun findAllWithStance(towards: io.dungeons.Id<io.dungeons.CoreEntity>, stance: FactionStance): List<Combatant> {
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
    fun listVisibleCombatants(observer: io.dungeons.Id<io.dungeons.CoreEntity>): List<Combatant>
    fun isVisibleTo(observer: io.dungeons.Id<io.dungeons.CoreEntity>, target: io.dungeons.Id<io.dungeons.CoreEntity>): Boolean
    fun listCombatantsInRange(observer: io.dungeons.Id<io.dungeons.CoreEntity>, rangeInFeet: io.dungeons.Feet): List<Combatant>
}

class SimpleCombatScenario(
    private val combatantsStore: CombatantsStore
) : CombatScenario {

    override fun listVisibleCombatants(observer: io.dungeons.Id<io.dungeons.CoreEntity>): List<Combatant> {
        return combatantsStore.listAll().filter { it.entity.id != observer }
    }

    override fun isVisibleTo(
        observer: io.dungeons.Id<io.dungeons.CoreEntity>,
        target: io.dungeons.Id<io.dungeons.CoreEntity>
    ): Boolean = true


    override fun listCombatantsInRange(
        observer: io.dungeons.Id<io.dungeons.CoreEntity>,
        rangeInFeet: io.dungeons.Feet
    ): List<Combatant> {
        val entity = combatantsStore.findOrNull(observer)?.entity
            ?: return emptyList()

        return combatantsStore.listAll()
            .filter { it.entity.id != observer }
            .filter { _root_ide_package_.io.dungeons.isInRange(entity.position, it.entity.position, rangeInFeet) }
    }

}