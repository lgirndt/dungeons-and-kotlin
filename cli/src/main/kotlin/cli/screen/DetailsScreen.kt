package cli.screen

import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.input.onInputEntered
import com.varabyte.kotter.foundation.input.setInput
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.RunScope
import com.varabyte.kotter.runtime.Session

class DetailsScreen(session: Session) : Screen<ScreenTransition>(
    session = session,
    ownTransition =  ScreenTransition.Details,
    defaultTransition =  ScreenTransition.Exit,
) {
    override val sectionBlock: MainRenderScope.() -> Unit = {
        textLine("Hallo, tell me some details")
        text("> ")
        input()
    }

    override val runBlock: RunScope.() -> Unit = {
        val scope = this
        onInputEntered {
            when (input) {
                "exit" -> exit(scope, ScreenTransition.Exit)
                "back" -> exit(scope, ScreenTransition.MyScreen)
            }
            setInput("")
        }

    }
}