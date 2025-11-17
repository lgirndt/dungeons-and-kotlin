package io.dungeons.combat

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

abstract class ActionCombatCommand : CombatCommand() {
    override fun consumeTurn(turn: Turn): Turn {
        return turn.useAction()
    }

    override fun isTurnAvailable(turn: Turn): Boolean = turn.actionAvailable
}

abstract class BonusActionCombatCommand : CombatCommand() {
    override fun consumeTurn(turn: Turn): Turn {
        return turn.useBonusAction()
    }

    override fun isTurnAvailable(turn: Turn): Boolean = turn.bonusActionAvailable
}
