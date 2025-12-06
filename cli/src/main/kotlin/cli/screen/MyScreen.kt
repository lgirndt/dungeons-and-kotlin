package cli.screen

import com.varabyte.kotter.foundation.collections.LiveList
import com.varabyte.kotter.foundation.collections.liveListOf
import com.varabyte.kotter.foundation.input.Completions
import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.setInput
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.foundation.timer.addTimer
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.RunScope
import com.varabyte.kotter.runtime.Session
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

data class ChatLine(val type: InputType, val text: String)

fun LiveList<ChatLine>.appendText(line: ChatLine) {
    this.withWriteLock {
        if (this.size >= 5) {
            this.removeAt(0)
        }
        this.add(line)
    }
}

class MyScreen(session: Session) :
    Screen<ScreenTransition>(
        session = session,
        ownTransition = ScreenTransition.MyScreen,
        defaultTransition = ScreenTransition.Details,
    ) {

    val history: LiveList<ChatLine> = session.liveListOf()

    override val sectionBlock: MainRenderScope.() -> Unit = {
        grid(
            Cols.Companion {
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
                textLine("Type 'quit' to exit, 'next' for details screen.")
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