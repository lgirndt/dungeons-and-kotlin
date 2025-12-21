package cli.ui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MenuItemTest {
    @Test
    fun `MenuItem with no children is a leaf`() {
        val item = MenuItem<String>(label = "Leaf Item", domainObject = "data")

        assertTrue(item.isLeaf)
        assertFalse(item.hasChildren)
        assertEquals("Leaf Item", item.label)
        assertEquals("data", item.domainObject)
    }

    @Test
    fun `MenuItem with children is a branch`() {
        val child1 = MenuItem<String>(label = "Child 1", domainObject = "child1")
        val child2 = MenuItem<String>(label = "Child 2", domainObject = "child2")
        val parent = MenuItem(
            label = "Parent",
            domainObject = null,
            children = listOf(child1, child2),
        )

        assertFalse(parent.isLeaf)
        assertTrue(parent.hasChildren)
        assertEquals(2, parent.children.size)
        assertNull(parent.domainObject)
    }

    @Test
    fun `MenuItem can have null domainObject for structural nodes`() {
        val item = MenuItem<String>(label = "Structural Node", domainObject = null)

        assertEquals("Structural Node", item.label)
        assertNull(item.domainObject)
        assertTrue(item.isLeaf)
    }

    @Test
    fun `MenuItem supports deep nesting`() {
        val leaf = MenuItem<Int>(label = "Leaf", domainObject = 3)
        val level2 = MenuItem(label = "Level 2", children = listOf(leaf))
        val level1 = MenuItem(label = "Level 1", children = listOf(level2))
        val root = MenuItem(label = "Root", children = listOf(level1))

        assertTrue(root.hasChildren)
        assertEquals(1, root.children.size)
        assertEquals("Level 1", root.children[0].label)
        assertEquals("Level 2", root.children[0].children[0].label)
        assertEquals("Leaf", root.children[0].children[0].children[0].label)
        assertEquals(3, root.children[0].children[0].children[0].domainObject)
    }

    @Test
    fun `MenuItem with empty children list is a leaf`() {
        val item = MenuItem<String>(label = "Item", domainObject = "data", children = emptyList())

        assertTrue(item.isLeaf)
        assertFalse(item.hasChildren)
    }

    @Test
    fun `MenuItem children list is immutable by default`() {
        val child = MenuItem<String>(label = "Child", domainObject = "child")
        val parent = MenuItem(label = "Parent", children = listOf(child))

        assertEquals(1, parent.children.size)
        // Verify children is a List (not MutableList)
        assertEquals(listOf(child), parent.children)
    }

    @Test
    fun `MenuItem supports different generic types`() {
        data class Action(val name: String, val target: String?)

        val exitItem = MenuItem(label = "Exit", domainObject = Action("exit", null))
        val navigateItem = MenuItem(label = "Go Home", domainObject = Action("navigate", "home"))

        assertEquals("exit", exitItem.domainObject?.name)
        assertEquals("home", navigateItem.domainObject?.target)
    }

    @Test
    fun `MenuItem can be created with default parameters`() {
        val item = MenuItem<String>(label = "Default Item")

        assertEquals("Default Item", item.label)
        assertNull(item.domainObject)
        assertTrue(item.children.isEmpty())
        assertTrue(item.isLeaf)
    }
}
