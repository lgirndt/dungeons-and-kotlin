package io.dungeons.domain.combat

import io.dungeons.domain.board.BoardPosition
import io.dungeons.domain.world.Square
import io.dungeons.domain.world.isInRange
import io.dungeons.port.CreatureId

interface CombatScenario : ProvidesBoardPosition {
    fun listVisibleCombatants(observer: CreatureId): List<Combatant>

    fun isVisibleTo(observer: CreatureId, target: CreatureId): Boolean

    fun listCombatantsInRange(observer: CreatureId, rangeInSquares: Square): List<Combatant>

    override fun getBoardPosition(creatureId: CreatureId): BoardPosition?
}

class SimpleCombatScenario(private val combatantsCollection: CombatantsCollection) : CombatScenario {
    override fun listVisibleCombatants(observer: CreatureId): List<Combatant> = combatantsCollection.listAll().filter {
        it.id != observer
    }

    override fun isVisibleTo(observer: CreatureId, target: CreatureId): Boolean = true

    override fun listCombatantsInRange(observer: CreatureId, rangeInSquares: Square): List<Combatant> {
        val position = getBoardPosition(observer)
            ?: error("No grid position found for creature $observer")

        return combatantsCollection
            .listAll()
            .filter { it.id != observer }
            .filter {
                getBoardPosition(it.creature.id)?.let { targetPos ->
                    isInRange(position, targetPos, rangeInSquares)
                } ?: true
            }
    }

    override fun getBoardPosition(creatureId: CreatureId): BoardPosition? {
        TODO("Not yet implemented")
    }
}
