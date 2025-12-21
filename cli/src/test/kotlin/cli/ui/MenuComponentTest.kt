package cli.ui

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for MenuComponent and MenuBuilder.
 *
 * These tests focus on the MenuBuilder DSL and MenuItem structure creation.
 * Full integration tests with keyboard input and rendering would require a running
 * Kotter session with VirtualTerminal.
 */
class MenuComponentTest {
    sealed class TestAction {
        data class Action1(val id: Int) : TestAction()

        data class Action2(val id: Int) : TestAction()

        data class Action3(val id: Int) : TestAction()
    }

    @Test
    fun `MenuBuilder creates simple menu structure with leaf nodes`() {
        val builder = MenuBuilder<TestAction>()
        val menu = builder.branch("Root") {
            listOf(
                leaf("Option 1", TestAction.Action1(1)),
                leaf("Option 2", TestAction.Action2(2)),
                leaf("Option 3", TestAction.Action3(3)),
            )
        }

        assertEquals("Root", menu.label)
        assertEquals(3, menu.children.size)
        assertEquals("Option 1", menu.children[0].label)
        assertEquals("Option 2", menu.children[1].label)
        assertEquals("Option 3", menu.children[2].label)
        assertTrue(menu.children[0].isLeaf)
        assertTrue(menu.children[1].isLeaf)
        assertTrue(menu.children[2].isLeaf)
    }

    @Test
    fun `MenuBuilder creates hierarchical menu structure`() {
        val builder = MenuBuilder<TestAction>()
        val menu = builder.branch("Root") {
            listOf(
                branch("Submenu 1") {
                    listOf(
                        leaf("Sub Option 1", TestAction.Action1(1)),
                        leaf("Sub Option 2", TestAction.Action2(2)),
                    )
                },
                leaf("Option 2", TestAction.Action3(3)),
            )
        }

        assertEquals("Root", menu.label)
        assertEquals(2, menu.children.size)

        val submenu = menu.children[0]
        assertEquals("Submenu 1", submenu.label)
        assertTrue(submenu.hasChildren)
        assertEquals(2, submenu.children.size)

        val leafOption = menu.children[1]
        assertEquals("Option 2", leafOption.label)
        assertTrue(leafOption.isLeaf)
    }

    @Test
    fun `MenuBuilder creates deep nesting structure`() {
        val builder = MenuBuilder<TestAction>()
        val menu = builder.branch("Root") {
            listOf(
                branch("Level 1") {
                    listOf(
                        branch("Level 2") {
                            listOf(
                                branch("Level 3") {
                                    listOf(
                                        leaf("Deep Option", TestAction.Action1(99)),
                                    )
                                },
                            )
                        },
                    )
                },
            )
        }

        assertEquals("Root", menu.label)
        val level1 = menu.children[0]
        assertEquals("Level 1", level1.label)
        val level2 = level1.children[0]
        assertEquals("Level 2", level2.label)
        val level3 = level2.children[0]
        assertEquals("Level 3", level3.label)
        val deepOption = level3.children[0]
        assertEquals("Deep Option", deepOption.label)
        assertEquals(99, (deepOption.domainObject as TestAction.Action1).id)
    }

    @Test
    fun `MenuBuilder leaf creates MenuItem with domain object`() {
        val builder = MenuBuilder<TestAction>()
        val item = builder.leaf("Test", TestAction.Action1(42))

        assertEquals("Test", item.label)
        assertNotNull(item.domainObject)
        assertEquals(42, (item.domainObject as TestAction.Action1).id)
        assertTrue(item.isLeaf)
        assertTrue(item.children.isEmpty())
    }

    @Test
    fun `MenuBuilder branch can have null domain object`() {
        val builder = MenuBuilder<TestAction>()
        val menu = builder.branch("Structural Node") {
            listOf(
                leaf("Child", TestAction.Action1(1)),
            )
        }

        assertEquals("Structural Node", menu.label)
        assertNull(menu.domainObject)
        assertTrue(menu.hasChildren)
    }

    @Test
    fun `MenuBuilder branch can have domain object`() {
        val builder = MenuBuilder<TestAction>()
        val action = TestAction.Action1(100)
        val menu = builder.branch("Branch with data", domainObject = action) {
            listOf(
                leaf("Child", TestAction.Action2(2)),
            )
        }

        assertEquals("Branch with data", menu.label)
        assertEquals(action, menu.domainObject)
        assertTrue(menu.hasChildren)
    }

    @Test
    fun `MenuBuilder supports different generic types`() {
        data class CustomAction(val name: String, val value: Int)

        val builder = MenuBuilder<CustomAction>()
        val menu = builder.branch("Root") {
            listOf(
                leaf("Action 1", CustomAction("first", 1)),
                leaf("Action 2", CustomAction("second", 2)),
            )
        }

        assertEquals(2, menu.children.size)
        assertEquals("first", menu.children[0].domainObject?.name)
        assertEquals(1, menu.children[0].domainObject?.value)
        assertEquals("second", menu.children[1].domainObject?.name)
        assertEquals(2, menu.children[1].domainObject?.value)
    }

    @Test
    fun `MenuConfig has correct default values`() {
        val config = MenuConfig()

        assertEquals("> ", config.selectedPrefix)
        assertEquals("  ", config.unselectedPrefix)
        assertEquals(true, config.showBreadcrumb)
        assertEquals(" > ", config.breadcrumbSeparator)
        assertNotNull(config.upKey)
        assertNotNull(config.downKey)
        assertNotNull(config.activateKey)
        assertNotNull(config.backKey)
    }

    @Test
    fun `MenuConfig can be customized`() {
        val config = MenuConfig(
            selectedPrefix = "* ",
            unselectedPrefix = "- ",
            showBreadcrumb = false,
            breadcrumbSeparator = " / ",
        )

        assertEquals("* ", config.selectedPrefix)
        assertEquals("- ", config.unselectedPrefix)
        assertEquals(false, config.showBreadcrumb)
        assertEquals(" / ", config.breadcrumbSeparator)
    }

    @Test
    fun `MenuBuilder can create empty branch`() {
        val builder = MenuBuilder<TestAction>()
        val menu = builder.branch("Empty") {
            emptyList()
        }

        assertEquals("Empty", menu.label)
        assertTrue(menu.children.isEmpty())
        assertTrue(menu.isLeaf)
        assertFalse(menu.hasChildren)
    }

    @Test
    fun `MenuBuilder creates complex realistic menu structure`() {
        val builder = MenuBuilder<TestAction>()
        val menu = builder.branch("What are you going to do?") {
            listOf(
                branch("Go To") {
                    listOf(
                        leaf("The northern door", TestAction.Action1(1)),
                        leaf("The western hallway", TestAction.Action1(2)),
                    )
                },
                branch("Open") {
                    listOf(
                        leaf("The trunk", TestAction.Action2(3)),
                    )
                },
                branch("Investigate") {
                    listOf(
                        leaf("The strange noise", TestAction.Action3(4)),
                        leaf("The dark passage", TestAction.Action3(5)),
                    )
                },
            )
        }

        assertEquals("What are you going to do?", menu.label)
        assertEquals(3, menu.children.size)

        assertEquals("Go To", menu.children[0].label)
        assertEquals(2, menu.children[0].children.size)

        assertEquals("Open", menu.children[1].label)
        assertEquals(1, menu.children[1].children.size)

        assertEquals("Investigate", menu.children[2].label)
        assertEquals(2, menu.children[2].children.size)
    }
}
