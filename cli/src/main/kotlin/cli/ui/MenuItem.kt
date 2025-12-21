package cli.ui

/**
 * Represents a single item in a hierarchical menu structure.
 *
 * @param T The type of domain object associated with this menu item
 * @property label The text to display for this menu item
 * @property domainObject Optional domain object associated with this item (null for structural nodes)
 * @property children Child menu items (empty for leaf nodes)
 */
data class MenuItem<T>(val label: String, val domainObject: T? = null, val children: List<MenuItem<T>> = emptyList()) {
    /**
     * Returns true if this item has no children (is a leaf node).
     */
    val isLeaf: Boolean get() = children.isEmpty()

    /**
     * Returns true if this item has children (is a branch node).
     */
    val hasChildren: Boolean get() = children.isNotEmpty()
}
