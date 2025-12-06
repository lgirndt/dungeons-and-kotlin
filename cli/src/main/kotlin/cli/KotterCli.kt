package cli

import cli.screen.DetailsScreen
import cli.screen.MyScreen
import cli.screen.Screen
import cli.screen.ScreenTransition
import com.varabyte.kotter.foundation.firstSuccess
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session
import com.varabyte.kotter.runtime.terminal.TerminalSize
import com.varabyte.kotter.terminal.system.SystemTerminal
import com.varabyte.kotter.terminal.virtual.VirtualTerminal
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

typealias ScreenMap = Map<ScreenTransition, Screen<ScreenTransition>>

@SpringBootApplication
class KotterCli {

    private val logger = LoggerFactory.getLogger(KotterCli::class.java)

    @Bean
    fun myScreen() = MyScreen()

    @Bean
    fun detailsScreen() = DetailsScreen()

    @Bean
    fun screens(
        myScreen: MyScreen,
        detailsScreen: DetailsScreen,
    )
        : ScreenMap {
        // It makes much more sense to build this lazy
        return listOf(
            myScreen,
            detailsScreen,
        ).associateBy { it.ownTransition }
    }


    @Bean
    fun runner(screens: ScreenMap) = CommandLineRunner {
        session(
            terminal = listOf(
                { SystemTerminal() },
                { VirtualTerminal.create(title = "D&D", terminalSize = TerminalSize(80, 40)) },
            ).firstSuccess(),
        ) {
            var transition = ScreenTransition.MyScreen
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
    runApplication<KotterCli>(*args)
}