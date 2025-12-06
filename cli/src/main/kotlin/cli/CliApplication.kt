package cli

import com.varabyte.kotter.foundation.firstSuccess
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.input.runUntilKeyPressed
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.foundation.timer.addTimer
import com.varabyte.kotter.runtime.terminal.TerminalSize
import com.varabyte.kotter.terminal.system.SystemTerminal
import com.varabyte.kotter.terminal.virtual.VirtualTerminal
import com.varabyte.kotterx.grid.Cols
import com.varabyte.kotterx.grid.GridCharacters
import com.varabyte.kotterx.grid.grid
import org.springframework.boot.CommandLineRunner
import org.springframework.web.client.RestClient
import kotlin.time.Duration.Companion.milliseconds

//@SpringBootApplication
class CliApplication {

//    @Bean
    fun restClient(): RestClient {
        return RestClient.builder().build()
    }

//    @Bean
    fun runner(restClient: RestClient) = CommandLineRunner {
//        println("CLI Application started! What do you want to do?")
//
//        print(">")
//        val input = readln()
//        println("You entered: $input")

        session(
            terminal = listOf(
//                { SystemTerminal() },
                { VirtualTerminal.create(title = "My App", terminalSize = TerminalSize(30, 30)) },
            ).firstSuccess(),
        ) {
            section { textLine("Hello, World") }.run()
        }

    }
}


fun main(args: Array<String>) {
//    runApplication<CliApplication>(*args)
    session(
        terminal = listOf(
            { SystemTerminal() },
            { VirtualTerminal.create(title = "My App", terminalSize = TerminalSize(80, 40)) },
        ).firstSuccess(),
    ) {
        section {
            textLine("Press ESC to quit")
            textLine()

            grid(
//                Cols { fit(); fixed(25) },
                Cols {
                    fixed(width - 25 - 3)
                    fixed(25)
                },
                characters = GridCharacters.INVISIBLE,
                targetWidth = width - 3,

                ) {
                // Header row
                cell {
                    // Fill the entire terminal height
                    repeat(height - 4) { line ->
                        textLine("Content at line $line")
                    }
                }
                cell { text("Right Column (25 chars)") }

            }
        }.runUntilKeyPressed(Keys.ESC) {
            // Periodically rerender to pick up size changes
            addTimer(100.milliseconds, repeat = true) {
                rerender()
            }
        }

    }
}
