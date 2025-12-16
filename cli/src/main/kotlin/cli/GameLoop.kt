package io.dungeons.cli

import com.varabyte.kotter.foundation.firstSuccess
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session
import com.varabyte.kotter.runtime.terminal.TerminalSize
import com.varabyte.kotter.terminal.system.SystemTerminal
import com.varabyte.kotter.terminal.virtual.VirtualTerminal
import io.dungeons.cli.screen.ScreenTransition
import io.dungeons.port.Id

private fun Session.clearScreen() {
    section {
        repeat(height) {
            textLine()
        }
    }.run()
}

class GameLoop(private val screens: ScreenMap, private val gameStateHolder: GameStateHolder) {

    fun run() {
        login(gameStateHolder)

        session(
            terminal = listOf(
                { SystemTerminal() },
                { VirtualTerminal.create(title = "D&D", terminalSize = TerminalSize(80, 40)) },
            ).firstSuccess(),
        ) {
            var transition = ScreenTransition.PickGame
            while (transition != ScreenTransition.Exit) {
                clearScreen()
                val screen = screens[transition] ?: break
                transition = screen.run(this)
            }
        }
    }

    private fun login(gameStateHolder: GameStateHolder) {
        val gameState = gameStateHolder.gameState
        gameStateHolder.gameState = gameState.copy(
            // TODO: we need a proper login flow
            playerId = Id.fromString("609cb790-d8b5-4a97-830f-0200fee465ab"),
        )
    }
}