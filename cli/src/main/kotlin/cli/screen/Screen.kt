package cli.screen

import com.varabyte.kotter.foundation.runUntilSignal
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.RunScope
import com.varabyte.kotter.runtime.Session

enum class ScreenTransition {
    Exit,
    Details,
    MyScreen
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