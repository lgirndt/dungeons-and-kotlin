package io.dungeons.cli

import com.varabyte.kotter.foundation.firstSuccess
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session
import com.varabyte.kotter.runtime.terminal.TerminalSize
import com.varabyte.kotter.terminal.system.SystemTerminal
import com.varabyte.kotter.terminal.virtual.VirtualTerminal
import io.dungeons.cli.screen.ScreenTransition
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

private fun Session.clearScreen() {
    section {
        repeat(height) {
            textLine()
        }
    }.run()
}

class GameLoop(private val screens: ScreenMap, private val gameStateHolder: GameStateHolder) {
    fun run(stateFile: Path?) {
        logger.debug { "Starting game loop" }
        login(gameStateHolder, stateFile)

        session(
            terminal = listOf(
                { SystemTerminal() },
                { VirtualTerminal.create(title = "D&D", terminalSize = TerminalSize(80, 40)) },
            ).firstSuccess(),
        ) {
            var transition = ScreenTransition.PickGame
            while (transition != ScreenTransition.Exit) {
                try {
                    clearScreen()
                    val screen = screens[transition] ?: break
                    transition = screen.run(this)
                } catch (
                    e:
                    @Suppress("TooGenericExceptionCaught")
                    Exception,
                ) {
                    logger.error(e) { "Error during screen transition: $transition" }
                    return@session
                }
            }
        }
    }

    private fun login(gameStateHolder: GameStateHolder, stateFile: Path?) {
        if (stateFile != null) {
            logger.debug { "State file provided: $stateFile, loading game state from file." }
            gameStateHolder.syncFromFile(stateFile)
        }
    }
}
