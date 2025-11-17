package io.dungeons.combat

import io.dungeons.CoreEntity
import io.dungeons.Id

interface CombatTrackerListener {
    fun rolledInitiative(combatants: List<Combatant>) {}
    fun startedCombat(combatants: List<Combatant>) {}
    fun startedTurn(turn: Turn, combatant: Combatant) {}
    fun skipTurn(combatant: Combatant) {}
    fun endTurn(turn: Turn, combatant: Combatant) {}
}

private val NOOP_COMBAT_TRACKER_LISTENER = object : CombatTrackerListener {}

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

data class TrackerEntry(
    val combatant: Combatant,
    val actor: TurnActor
)

class CombatTracker(
    trackerEntries: Collection<TrackerEntry>,
    val combatScenario: CombatScenario,
    val listener: CombatTrackerListener = NOOP_COMBAT_TRACKER_LISTENER
) {
    internal val combatantsOrderedByInitiative: List<Combatant> = trackerEntries
        .map(TrackerEntry::combatant)
        .sortedByDescending { it.initiative }
        .also { listener.rolledInitiative(it) }

    private val actors: Map<Id<CoreEntity>, TurnActor> = trackerEntries.associate{ it.combatant.id to it.actor }
    private val turnTable : MutableMap<Id<CoreEntity>, Turn> = mutableMapOf()

    init {
        require(trackerEntries.count() >= 2) { "At least two combatants are required to start combat." }
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