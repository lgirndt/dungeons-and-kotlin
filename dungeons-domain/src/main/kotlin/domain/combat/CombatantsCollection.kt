package io.dungeons.domain.combat

import io.dungeons.port.CreatureId

class CombatantsCollection(
    combatants: Collection<Combatant>,
    nonHostileFactionRelationships: List<FactionRelationship> = emptyList(),
) {
    val combatants: Map<CreatureId, Combatant> = combatants.associateBy { it.id }

    val factionRelations: FactionRelations = nonHostileFactionRelationships
        .onEach {
            require(it.stance == FactionStance.Hostile) {
                "CombatantsStore can only accept non-hostile relationships."
            }
        }
        .fold(FactionRelations.Builder()) { builder, relationship ->
            builder.add(relationship)
        }
        .build()

    operator fun get(id: CreatureId): Combatant? = combatants[id]

    fun findAllWithStance(towards: CreatureId, stance: FactionStance): List<Combatant> {
        val combatant = combatants[towards] ?: return emptyList()
        val targetFaction = combatant.faction
        return combatants.values.filter {
            val relationStance = factionRelations.queryStance(targetFaction, it.faction)
            relationStance == stance
        }
    }

    fun listAll(): List<Combatant> = combatants.values.toList()
}
