package io.dungeons.domain.combat

import io.dungeons.domain.Creature
import io.dungeons.domain.board.BoardPosition
import io.dungeons.domain.core.Id
import io.dungeons.domain.world.Square
import io.dungeons.domain.world.isInRange

interface CombatScenario : ProvidesBoardPosition {
    fun listVisibleCombatants(observer: Id<Creature>): List<Combatant>

    fun isVisibleTo(observer: Id<Creature>, target: Id<Creature>): Boolean

    fun listCombatantsInRange(observer: Id<Creature>, rangeInSquares: Square): List<Combatant>

    override fun getBoardPosition(creatureId: Id<Creature>): BoardPosition?
}

class SimpleCombatScenario(private val combatantsCollection: CombatantsCollection) : CombatScenario {
    override fun listVisibleCombatants(observer: Id<Creature>): List<Combatant> =
        combatantsCollection.listAll().filter {
            it.id != observer
        }

    override fun isVisibleTo(observer: Id<Creature>, target: Id<Creature>): Boolean = true

    override fun listCombatantsInRange(observer: Id<Creature>, rangeInSquares: Square): List<Combatant> {
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

    override fun getBoardPosition(creatureId: Id<Creature>): BoardPosition? {
        TODO("Not yet implemented")
    }
}
