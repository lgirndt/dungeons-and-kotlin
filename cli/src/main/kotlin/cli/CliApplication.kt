package cli

import com.varabyte.kotter.foundation.firstSuccess
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.input.runUntilKeyPressed
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.terminal.TerminalSize
import com.varabyte.kotter.terminal.system.SystemTerminal
import com.varabyte.kotter.terminal.virtual.VirtualTerminal
import com.varabyte.kotterx.grid.Cols
import com.varabyte.kotterx.grid.GridCharacters
import com.varabyte.kotterx.grid.grid
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestClient

@SpringBootApplication
class CliApplication {

    @Bean
    fun restClient(): RestClient {
        return RestClient.builder().build()
    }

    @Bean
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
                Cols { star(); fixed(25) },
                characters = GridCharacters.CURVED
            ) {
                // Header row
                cell { text("Left Column (fills space)") }
                cell { text("Right Column (25 chars)") }
                
                // Content rows
                cell { text("This column expands to fill the remaining terminal width") }
                cell { text("This is fixed at 25") }
                
                cell { text("More content here...") }
                cell { text("Sidebar info") }
                
                cell { text("You can add as many rows as you need") }
                cell { text("Status: Ready") }
            }
        }.runUntilKeyPressed(Keys.ESC)

    }
}
