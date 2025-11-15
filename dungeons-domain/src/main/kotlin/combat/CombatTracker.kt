package io.dungeons.combat

import com.google.common.collect.ImmutableListMultimap
import io.dungeons.CoreEntity
import io.dungeons.Id

interface CombatTrackerListener {
    fun rolledInitiative(combatants: List<Combatant>) {}
    fun startedCombat(combatants: List<Combatant>) {}
    fun startedTurn(combatant: Combatant) {}
}

abstract class CombatCommand {

    fun perform(turn: Turn, combatScenario: CombatScenario) {
        require(isTurnAvailable(turn)) { "Already performed in this turn." }
        doPerform(combatScenario)
        consumeTurn(turn)
    }

    protected abstract fun doPerform(combatScenario: CombatScenario)
    protected abstract fun consumeTurn(turn: Turn)
    protected abstract fun isTurnAvailable(turn: Turn): Boolean
}

abstract class MovementCombatCommand : CombatCommand() {
    override fun consumeTurn(turn: Turn) =
        turn.useMovement()

    override fun isTurnAvailable(turn: Turn): Boolean = turn.movementAvailable
}

private val NOOP_COMBAT_TRACKER_LISTENER = object : CombatTrackerListener {}

class Turn(
    movementAvailable: Boolean = true,
    actionAvailable: Boolean = true,
    bonusActionAvailable: Boolean = true,
    reactionAvailable: Boolean = true
) {
    var movementAvailable: Boolean = movementAvailable
        private set
    var actionAvailable: Boolean = actionAvailable
        private set
    var bonusActionAvailable: Boolean = bonusActionAvailable
        private set
    var reactionAvailable: Boolean = reactionAvailable
        private set

    val hasOptionsLeft: Boolean
        get() = movementAvailable || actionAvailable || bonusActionAvailable || reactionAvailable

    fun useMovement() {
        require(movementAvailable) { "Movement already used this turn." }
        movementAvailable = false
    }

    fun useAction() {
        require(actionAvailable) { "Action already used this turn." }
        actionAvailable = false
    }

    fun useBonusAction() {
        require(bonusActionAvailable) { "Bonus action already used this turn." }
        bonusActionAvailable = false
    }

    fun useReaction() {
        require(reactionAvailable) { "Reaction already used this turn." }
        reactionAvailable = false
    }

}

interface TurnActor {
    fun handleTurn(combatant: Combatant, turn: Turn, combatScenario: CombatScenario): CombatCommand?
}

class CombatTracker(
    combatants: Collection<Combatant>,
    nonHostileFactionRelationships: List<FactionRelationship> = emptyList(),
    combatScenarioFactory: (CombatantsStore) -> CombatScenario  = { SimpleCombatScenario(it) },
    val actors: Map<Id<CoreEntity>, TurnActor> = emptyMap(), // TODO
    val listener: CombatTrackerListener = NOOP_COMBAT_TRACKER_LISTENER
) {
    val combatantsOrderedByInitiative: List<Combatant> = combatants
        .sortedByDescending { it.initiative }
        .also { listener.rolledInitiative(it) }

    val combatantsStore : CombatantsStore = CombatantsStore(
        combatantsByFaction = ImmutableListMultimap.builder<Faction, CoreEntity>()
            .apply {
                combatants.forEach { combatant ->
                    put(combatant.faction, combatant.entity)
                }
            }
            .build(),
        nonHostileFactionRelationships = nonHostileFactionRelationships
    )

    private val combatScenario: CombatScenario = combatScenarioFactory(combatantsStore)

    init {
        require(combatants.count() >= 2) { "At least two combatants are required to start combat." }
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
        val turn = Turn()
        do {
            val command = actor.handleTurn(currentCombatant, turn, combatScenario) ?: break
            command.perform(turn, combatScenario)

        }while(turn.hasOptionsLeft)


        if (turnIndex >= combatantsOrderedByInitiative.size) {
            turnIndex = 0
            round++
        }
    }
}