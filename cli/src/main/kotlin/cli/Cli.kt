package io.dungeons.cli

import com.varabyte.kotter.foundation.firstSuccess
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session
import com.varabyte.kotter.runtime.terminal.TerminalSize
import com.varabyte.kotter.terminal.system.SystemTerminal
import com.varabyte.kotter.terminal.virtual.VirtualTerminal
import io.dungeons.cli.screen.DetailsScreen
import io.dungeons.cli.screen.MyScreen
import io.dungeons.cli.screen.PickAdventureScreen
import io.dungeons.cli.screen.PickGameScreen
import io.dungeons.cli.screen.RoomScreen
import io.dungeons.cli.screen.Screen
import io.dungeons.cli.screen.ScreenTransition
import io.dungeons.domain.core.Id
import io.dungeons.domain.core.Player
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import kotlin.time.Clock

typealias ScreenMap = Map<ScreenTransition, Screen<ScreenTransition>>

@SpringBootApplication(scanBasePackages = ["io.dungeons"])
class Cli {
    private val logger = LoggerFactory.getLogger(Cli::class.java)

    @Bean
    fun clock() = Clock.System

    @Bean
    fun screens(
        myScreen: MyScreen,
        detailsScreen: DetailsScreen,
        pickAdventureScreen: PickAdventureScreen,
        pickGameScreen: PickGameScreen,
        roomScreen: RoomScreen,
    ): ScreenMap {
        // It makes much more sense to build this lazy
        return listOf(
            myScreen,
            detailsScreen,
            pickAdventureScreen,
            pickGameScreen,
            roomScreen,
        ).associateBy { it.ownTransition }
    }

    private fun login(gameStateHolder: GameStateHolder) {
        val gameState = gameStateHolder.gameState
        gameStateHolder.gameState = gameState.copy(
            // TODO: we need a proper login flow
            player = Player(Id.fromString("609cb790-d8b5-4a97-830f-0200fee465ab")),
        )
    }

    @Bean
    fun runner(screens: ScreenMap, gameStateHolder: GameStateHolder) = CommandLineRunner {
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
}

private fun Session.clearScreen() {
    section {
        repeat(height) {
            textLine()
        }
    }.run()
}

fun main(args: Array<String>) {
    // Otherwise the VirtualTerminal may fail to initialize on some systems
    System.setProperty("java.awt.headless", "false")
    runApplication<Cli>(*args)
}
