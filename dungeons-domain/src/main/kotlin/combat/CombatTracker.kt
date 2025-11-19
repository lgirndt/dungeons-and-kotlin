package io.dungeons.combat

import io.dungeons.CoreEntity
import io.dungeons.core.Id

interface CombatTrackerListener {
    fun rolledInitiative(combatants: List<Combatant>) {}
    fun startedCombat(combatants: List<Combatant>) {}
    fun startedTurn(turn: Turn, combatant: Combatant) {}
    fun skipTurn(combatant: Combatant) {}
    fun endTurn(turn: Turn, combatant: Combatant) {}
}

private val NOOP_COMBAT_TRACKER_LISTENER = object : CombatTrackerListener {}

class CombatTracker(
    combatants: Collection<Combatant>,
    val combatScenario: CombatScenario,
    val listener: CombatTrackerListener = NOOP_COMBAT_TRACKER_LISTENER
) {
    internal val combatantsOrderedByInitiative: List<Combatant> = combatants
        .sortedByDescending { it.initiative }
        .also { listener.rolledInitiative(it) }

    private val actors: Map<Id<CoreEntity>, TurnActor> = combatants.associate { it.id to it.actor }
    private val turnTable : MutableMap<Id<CoreEntity>, Turn> = mutableMapOf()

    init {
        require(combatants.count() >= 2) { "At least two combatants are required to start combat." }
    }

    private var round = 1
    private var turnIndex = 0


    fun advanceTurn() {
        handleStartCombat()

        val currentCombatant = combatantsOrderedByInitiative[turnIndex]
        if (currentCombatant.hitPoints > 0) {
            val actor = actors.getOrElse(currentCombatant.id) {
                error("No actor found for combatant ${currentCombatant.id}")
            }
            var turn = nextTurnFor(currentCombatant)
            do {
                val command = actor.handleTurn(currentCombatant, turn, combatScenario)
                    ?: break
                turn = command.perform(turn, combatScenario)

            } while (turn.hasOptionsForTurnLeft)
        } else {
            listener.skipTurn(currentCombatant)// Combatant is down, skip turn
        }

        incrementTurn()
    }

    private fun handleStartCombat() {
        if (turnIndex == 0 && round == 1) {
            listener.startedCombat(combatantsOrderedByInitiative)
        }
    }

    private fun incrementTurn() {
        turnIndex++
        if (turnIndex >= combatantsOrderedByInitiative.size) {
            turnIndex = 0
            round++
        }
    }

    private fun nextTurnFor(combatant: Combatant): Turn {
        val id = combatant.id
        turnTable[id]?.let {
            listener.endTurn(it, combatant)
        }
        val newTurn = Turn(round)
        listener.startedTurn(newTurn, combatant)
        turnTable[id] = newTurn
        return newTurn
    }
}