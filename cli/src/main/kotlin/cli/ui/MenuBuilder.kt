package cli.ui

/**
 * DSL builder for constructing menu hierarchies.
 *
 * Provides a simple, declarative API for creating menu structures:
 * ```kotlin
 * MenuBuilder<MyAction>().apply {
 *     branch("Main Menu") {
 *         listOf(
 *             branch("Submenu") {
 *                 listOf(
 *                     leaf("Option 1", MyAction.Option1),
 *                     leaf("Option 2", MyAction.Option2)
 *                 )
 *             },
 *             leaf("Exit", MyAction.Exit)
 *         )
 *     }
 * }
 * ```
 *
 * @param T The type of domain object associated with menu items
 */
class MenuBuilder<T> {
    /**
     * Creates a menu item with children (branch node).
     *
     * @param label The display text for this menu item
     * @param domainObject Optional domain object associated with this branch (null by default)
     * @param children Builder lambda that returns the list of child menu items
     * @return A MenuItem with the specified children
     */
    fun branch(label: String, domainObject: T? = null, children: MenuBuilder<T>.() -> List<MenuItem<T>>): MenuItem<T> {
        val builder = MenuBuilder<T>()
        val childList = builder.children()
        return MenuItem(label, domainObject, childList)
    }

    /**
     * Creates a leaf menu item (no children).
     *
     * @param label The display text for this menu item
     * @param domainObject The domain object associated with this leaf item
     * @return A MenuItem with no children
     */
    fun leaf(label: String, domainObject: T): MenuItem<T> = MenuItem(label, domainObject, emptyList())
}
