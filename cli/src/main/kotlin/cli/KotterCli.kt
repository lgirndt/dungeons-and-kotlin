package cli

import com.varabyte.kotter.foundation.collections.LiveList
import com.varabyte.kotter.foundation.collections.liveListOf
import com.varabyte.kotter.foundation.firstSuccess
import com.varabyte.kotter.foundation.input.Completions
import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.setInput
import com.varabyte.kotter.foundation.runUntilSignal
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.foundation.timer.addTimer
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.RunScope
import com.varabyte.kotter.runtime.Session
import com.varabyte.kotter.runtime.terminal.TerminalSize
import com.varabyte.kotter.terminal.system.SystemTerminal
import com.varabyte.kotter.terminal.virtual.VirtualTerminal
import com.varabyte.kotterx.grid.Cols
import com.varabyte.kotterx.grid.GridCharacters
import com.varabyte.kotterx.grid.grid
import kotlin.time.Duration.Companion.milliseconds


val someLines = listOf(
    "The quick brown fox jumps over the lazy dog.",
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
    "In a village of La Mancha, the name of which I have no desire to call to mind...",
    "It was the best of times, it was the worst of times...",
    "All happy families are alike; each unhappy family is unhappy in its own way.",
)

enum class InputType {
    System,
    User
}

enum class ScreenTransition {
    Exit,
    Details,
    MyScreen
}

data class ChatLine(val type: InputType, val text: String)

fun LiveList<ChatLine>.appendText(line: ChatLine): Unit {
    this.withWriteLock {
        if (this.size >= 5) {
            this.removeAt(0)
        }
        this.add(line)
    }
}

abstract class Screen<T>(
    protected val session: Session,
    defaultTransition: T,
) {

    private var transition: T = defaultTransition

    abstract protected val sectionBlock: MainRenderScope.() -> Unit
    abstract protected val runBlock: RunScope.() -> Unit

    protected fun exit(scope: RunScope, nextScreen: T) {
        transition = nextScreen
        scope.signal()
    }

    fun run(): T {
        session.section(sectionBlock).runUntilSignal(runBlock)
        return transition
    }
}

class MyScreen(session: Session) : Screen<ScreenTransition>(session, ScreenTransition.Details) {

    val history: LiveList<ChatLine> = session.liveListOf()

    override val sectionBlock: MainRenderScope.() -> Unit = {
        grid(
            Cols {
                fixed(width - 2)
            },
            characters = GridCharacters.INVISIBLE,
        ) {
            cell(0, 0) {
                textLine("hello")
            }
            cell(1, 0) {
                for (line in history) {
                    when (line.type) {
                        InputType.System -> textLine("\uD83E\uDD16: ${line.text}")
                        InputType.User -> textLine("\uD83D\uDC69\u200D\uFE0F: ${line.text}")
                    }
                }
            }
            cell(2, 0) {
                textLine("Press ESC to quit")
            }
        }

        text("> ")
        input(Completions("Lord Peter Wimsey", "Sherlock Holmes", "Hercule Poirot"))
    }


    override val runBlock: RunScope.() -> Unit = {

        val scope = this

        // Periodically rerender to pick up size changes
        addTimer(4000.milliseconds, repeat = true) {
            history.appendText(ChatLine(InputType.System, someLines.random()))
        }
        onInputEntered {

            when (input) {
                "exit", "quit" -> {
                    exit(scope, ScreenTransition.Exit)
                }

                "next" -> {
                    exit(scope, ScreenTransition.Details)
                }

                else -> {
                    history.appendText(ChatLine(InputType.User, input))
                }
            }
            setInput("")
        }
    }
}

fun main() {
    session(
        terminal = listOf(
            { SystemTerminal() },
            { VirtualTerminal.create(title = "My App", terminalSize = TerminalSize(80, 40)) },
        ).firstSuccess(),
    ) {
        var transition = ScreenTransition.MyScreen
        while (transition != ScreenTransition.Exit) {
            val screen = when (transition) {
                ScreenTransition.MyScreen -> MyScreen(this)
                ScreenTransition.Details -> MyScreen(this)
                ScreenTransition.Exit -> break
            }
            transition = screen.run()
        }
    }

}