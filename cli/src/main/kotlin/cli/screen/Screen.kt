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
    private var internalValue: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T =
        internalValue ?: error("Property ${property.name} accessed before initialization")

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        if (this@InitOnce.internalValue != null) {
            error("Property ${property.name} is already initialized and cannot be set again")
        }
        this@InitOnce.internalValue = value
    }
}

abstract class Screen<T>(
    // protected val session: Session,
    val ownTransition: T,
    defaultTransition: T,
) {
    private var transition: T = defaultTransition
    private var isInitialized = false

    /**
     * Defines the rendering/display logic for this screen.
     *
     * Use this to:
     * - Display text, menus, and UI elements using Kotter's rendering DSL
     * - Access screen state variables to render dynamic content
     * - Set up the visual layout that will be shown to the user
     *
     * This block is called on each render cycle. Keep it focused on presentation logic only.
     * Do NOT perform user input handling here - use [runBlock] for that.
     *
     * Example implementation:
     * ```
     * override val sectionBlock: MainRenderScope.() -> Unit = {
     *     text("Welcome to ${adventureName}")
     *     text("Current room: ${currentRoom.name}")
     *     textLine()
     *     text("Select an option:")
     * }
     * ```
     */
    protected abstract val sectionBlock: MainRenderScope.() -> Unit

    /**
     * Defines the interactive/event loop logic for this screen.
     *
     * Use this to:
     * - Handle user input (keyboard events, etc.)
     * - Update screen state based on user actions
     * - Call [exit] to transition to another screen when ready
     *
     * This block runs in a loop until [exit] is called, which signals the loop to stop.
     * The screen will continue to re-render (using [sectionBlock]) as state changes.
     *
     * Example implementation:
     * ```
     * override val runBlock: RunScope.() -> Unit = {
     *     onKeyPressed {
     *         when (key) {
     *             Keys.DIGIT_1 -> {
     *                 selectedOption = 1
     *                 exit(this, ScreenTransition.Details)
     *             }
     *             Keys.Q -> exit(this, ScreenTransition.Exit)
     *         }
     *     }
     * }
     * ```
     */
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
