package io.dungeons.cli.screen

import com.varabyte.kotter.foundation.runUntilSignal
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.RunScope
import com.varabyte.kotter.runtime.Session
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

enum class ScreenTransition {
    Exit,
    Details,
    MyScreen,
    PickAdventure,
    Room,
}

/**
 * Property delegate for properties that should be initialized exactly once.
 * Provides better error messages than lateinit and prevents re-initialization.
 */
class InitOnce<T> : ReadWriteProperty<Any?, T> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
        value ?: error("Property ${property.name} accessed before initialization")

    override fun setValue(thisRef: Any?, property: KProperty<*>, newValue: T) {
        if (value != null) {
            error("Property ${property.name} is already initialized and cannot be set again")
        }
        value = newValue
    }
}

abstract class Screen<T>(
    // protected val session: Session,
    val ownTransition: T,
    defaultTransition: T,
) {
    private var transition: T = defaultTransition
    private var isInitialized = false

    protected abstract val sectionBlock: MainRenderScope.() -> Unit
    protected abstract val runBlock: RunScope.() -> Unit

    protected abstract fun init(session: Session)

    protected fun exit(scope: RunScope, nextScreen: T) {
        transition = nextScreen
        scope.signal()
    }

    fun run(session: Session): T {
        if (!isInitialized) {
            init(session)
            isInitialized = true
        }
        session.section(sectionBlock).runUntilSignal(runBlock)
        return transition
    }
}
