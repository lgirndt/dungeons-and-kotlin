package cli

import cli.screen.DetailsScreen
import cli.screen.MyScreen
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

@SpringBootApplication
class KotterCli {

    private val logger = LoggerFactory.getLogger(KotterCli::class.java)

    @Bean
    fun runner() = CommandLineRunner {
        println("CLI Application started! What do you want to do?")
        session(
            terminal = listOf(
                { SystemTerminal() },
                { VirtualTerminal.create(title = "My App", terminalSize = TerminalSize(80, 40)) },
            ).firstSuccess(),
        ) {
            val screens = listOf(
                MyScreen(),
                DetailsScreen(),
            ).associateBy { it.ownTransition }

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
    System.setProperty("java.awt.headless", "false")
    runApplication<KotterCli>(*args)
//    session(
//        terminal = listOf(
//            { SystemTerminal() },
//            { VirtualTerminal.create(title = "My App", terminalSize = TerminalSize(80, 40)) },
//        ).firstSuccess(),
//    ) {
//
//        val screens = listOf(
//            MyScreen(this),
//            DetailsScreen(this),
//        ).associateBy { it.ownTransition }
//
//        var transition = ScreenTransition.MyScreen
//        while (transition != ScreenTransition.Exit) {
//            clearScreen()
//            val screen = screens[transition] ?:
//                break
//
//            transition = screen.run()
//        }
//    }

}