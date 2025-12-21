package cli.ui

import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys

/**
 * Configuration options for menu appearance and behavior.
 *
 * @property selectedPrefix Prefix shown for selected items (default: "> ")
 * @property unselectedPrefix Prefix shown for unselected items (default: "  ")
 * @property upKey Key to move selection up (default: Keys.UP)
 * @property downKey Key to move selection down (default: Keys.DOWN)
 * @property activateKey Key to activate selected item (default: Keys.ENTER)
 * @property backKey Optional key to go back to parent level (default: Keys.ESC)
 * @property showBreadcrumb Whether to show current path in hierarchy (default: true)
 * @property breadcrumbSeparator Separator for breadcrumb display (default: " > ")
 */
data class MenuConfig(
    val selectedPrefix: String = "> ",
    val unselectedPrefix: String = "  ",
    val upKey: Key = Keys.UP,
    val downKey: Key = Keys.DOWN,
    val activateKey: Key = Keys.ENTER,
    val backKey: Key? = Keys.ESC,
    val showBreadcrumb: Boolean = true,
    val breadcrumbSeparator: String = " > ",
)
