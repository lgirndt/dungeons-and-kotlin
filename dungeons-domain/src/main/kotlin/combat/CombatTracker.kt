package io.dungeons.combat

import io.dungeons.CoreEntity
import io.dungeons.Id

interface CombatTrackerListener {
    fun rolledInitiative(combatants: List<Combatant>) {}
    fun startedCombat(combatants: List<Combatant>) {}
    fun startedTurn(combatant: Combatant) {}
}

abstract class CombatCommand {

    fun perform(turn: Turn, combatScenario: CombatScenario): Turn {
        require(isTurnAvailable(turn)) { "Already performed in this turn." }
        doPerform(combatScenario)
        return consumeTurn(turn)
    }

    protected abstract fun doPerform(combatScenario: CombatScenario)
    protected abstract fun consumeTurn(turn: Turn): Turn
    protected abstract fun isTurnAvailable(turn: Turn): Boolean
}

abstract class MovementCombatCommand : CombatCommand() {
    override fun consumeTurn(turn: Turn): Turn {
        return turn.useMovement()
    }


    override fun isTurnAvailable(turn: Turn): Boolean = turn.movementAvailable
}

private val NOOP_COMBAT_TRACKER_LISTENER = object : CombatTrackerListener {}

data class Turn(
    val movementAvailable: Boolean = true,
    val actionAvailable: Boolean = true,
    val bonusActionAvailable: Boolean = true,
    val reactionAvailable: Boolean = true
) {

    val hasOptionsLeft: Boolean
        get() = movementAvailable
                || actionAvailable
                || bonusActionAvailable
                || reactionAvailable

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

    val actors: Map<Id<CoreEntity>, TurnActor> = trackerEntries.associate{ it.combatant.id to it.actor }

    init {
        require(trackerEntries.count() >= 2) { "At least two combatants are required to start combat." }
    }

    private var round = 1
    private var turnIndex = 0


    fun advanceTurn() {
        if (turnIndex == 0 && round == 1) {
            listener.startedCombat(combatantsOrderedByInitiative)
        }

        val currentCombatant = combatantsOrderedByInitiative[turnIndex]
        turnIndex++

        listener.startedTurn(currentCombatant)
        val actor = actors.getOrElse(currentCombatant.id) {
            throw IllegalStateException("No actor found for combatant ${currentCombatant.id}")
        }
        // TODO needs to live longer
        var turn = Turn()
        do {
            val command = actor.handleTurn(currentCombatant, turn, combatScenario)
                ?: break
            turn = command.perform(turn, combatScenario)

        } while (turn.hasOptionsLeft)


        if (turnIndex >= combatantsOrderedByInitiative.size) {
            turnIndex = 0
            round++
        }
    }
}