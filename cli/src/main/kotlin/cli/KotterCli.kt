package cli

import cli.screen.MyScreen
import cli.screen.ScreenTransition
import com.varabyte.kotter.foundation.collections.LiveList
import com.varabyte.kotter.foundation.firstSuccess
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.terminal.TerminalSize
import com.varabyte.kotter.terminal.system.SystemTerminal
import com.varabyte.kotter.terminal.virtual.VirtualTerminal

fun main() {
    session(
        terminal = listOf(
            { SystemTerminal() },
            { VirtualTerminal.create(title = "My App", terminalSize = TerminalSize(80, 40)) },
        ).firstSuccess(),
    ) {
        var transition = ScreenTransition.MyScreen
        while (transition != ScreenTransition.Exit) {
            section {
                repeat(height) {
                    textLine()
                }
            }.run()

            val screen = when (transition) {
                ScreenTransition.MyScreen -> MyScreen(this)
                ScreenTransition.Details -> MyScreen(this)
                ScreenTransition.Exit -> break
            }
            transition = screen.run()
        }
    }

}