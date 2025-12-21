package cli.ui

import com.varabyte.kotter.foundation.LiveVar
import com.varabyte.kotter.foundation.collections.LiveList
import com.varabyte.kotter.foundation.collections.liveListOf
import com.varabyte.kotter.foundation.input.onKeyPressed
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.RunScope
import com.varabyte.kotter.runtime.Session

/**
 * Reusable hierarchical menu component for Kotter-based CLI applications.
 *
 * This component provides a navigation interface for hierarchical menu structures,
 * supporting unlimited nesting levels, configurable keyboard bindings, and customizable
 * appearance.
 *
 * Example usage:
 * ```kotlin
 * val menu = MenuComponent.create(
 *     session = session,
 *     config = MenuConfig(),
 *     onActivate = { action -> handleAction(action) }
 * ) {
 *     branch("Main Menu") {
 *         listOf(
 *             branch("Submenu") {
 *                 listOf(
 *                     leaf("Option 1", MyAction.Option1),
 *                     leaf("Option 2", MyAction.Option2)
 *                 )
 *             }
 *         )
 *     }
 * }
 * ```
 *
 * @param T The type of domain object associated with menu items
 */
class MenuComponent<T> private constructor(
    private val session: Session,
    private val rootItem: MenuItem<T>,
    private val config: MenuConfig,
    private val onActivate: (T) -> Unit,
) {
    // Reactive state
    private val navigationStack: LiveList<MenuItem<T>> = session.liveListOf(rootItem)
    private val selectedIndex: LiveVar<Int> = session.liveVarOf(0)

    // Current state accessors
    private val currentNode: MenuItem<T> get() = navigationStack.last()
    private val currentChildren: List<MenuItem<T>> get() = currentNode.children
    private val selectedItem: MenuItem<T>? get() = currentChildren.getOrNull(selectedIndex.value)

    /**
     * Renders the menu in the given MainRenderScope.
     * Call this from your screen's sectionBlock.
     */
    fun render(scope: MainRenderScope) {
        with(scope) {
            // Render breadcrumb if enabled
            if (config.showBreadcrumb && navigationStack.size > 1) {
                val breadcrumb = navigationStack.drop(1).joinToString(config.breadcrumbSeparator) { it.label }
                textLine(breadcrumb)
                textLine()
            }

            // Render current menu items
            currentChildren.forEachIndexed { index, item ->
                val prefix = if (index == selectedIndex.value) {
                    config.selectedPrefix
                } else {
                    config.unselectedPrefix
                }
                textLine("$prefix${item.label}")
            }
        }
    }

    /**
     * Handles keyboard input in the given RunScope.
     * Call this from your screen's runBlock.
     */
    fun handleInput(scope: RunScope) {
        with(scope) {
            onKeyPressed {
                when (key) {
                    config.upKey -> selectPrevious()
                    config.downKey -> selectNext()
                    config.activateKey -> activateSelected()
                    config.backKey -> navigateUp()
                }
            }
        }
    }

    private fun navigateDown(item: MenuItem<T>) {
        if (item.hasChildren) {
            navigationStack.add(item)
            selectedIndex.value = 0
        }
    }

    /**
     * Navigates back to parent level.
     * @return true if navigation occurred, false if already at root
     */
    fun navigateUp(): Boolean = if (navigationStack.size > 1) {
        navigationStack.removeAt(navigationStack.lastIndex)
        selectedIndex.value = 0
        true
    } else {
        false
    }

    private fun selectPrevious() {
        if (currentChildren.isEmpty()) return
        selectedIndex.value = if (selectedIndex.value > 0) {
            selectedIndex.value - 1
        } else {
            currentChildren.lastIndex
        }
    }

    private fun selectNext() {
        if (currentChildren.isEmpty()) return
        selectedIndex.value = if (selectedIndex.value < currentChildren.lastIndex) {
            selectedIndex.value + 1
        } else {
            0
        }
    }

    private fun activateSelected() {
        val item = selectedItem ?: return
        if (item.hasChildren) {
            navigateDown(item)
        } else {
            item.domainObject?.let { onActivate(it) }
        }
    }

    companion object {
        /**
         * Creates a new MenuComponent with a builder-style DSL.
         *
         * @param session The Kotter session
         * @param config Configuration options (default: MenuConfig())
         * @param onActivate Callback invoked when a leaf item is activated
         * @param buildMenu Builder lambda to construct the menu structure
         */
        fun <T> create(
            session: Session,
            config: MenuConfig = MenuConfig(),
            onActivate: (T) -> Unit,
            buildMenu: MenuBuilder<T>.() -> MenuItem<T>,
        ): MenuComponent<T> {
            val builder = MenuBuilder<T>()
            val rootItem = builder.buildMenu()
            require(rootItem.hasChildren) { "Root menu item must have children" }
            return MenuComponent(session, rootItem, config, onActivate)
        }
    }
}
