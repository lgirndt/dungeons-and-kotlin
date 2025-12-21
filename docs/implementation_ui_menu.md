# Implementation Plan: Reusable UI Menu Component

This document outlines the implementation plan for a reusable hierarchical menu component for the Kotter-based CLI application.

## 1. Overview and Goals

### Objectives
- Create a reusable, generic menu component that can be embedded in any Screen
- Support hierarchical navigation (parent-child relationships)
- Eliminate code duplication across screens (PickGameScreen, PickAdventureScreen, etc.)
- Provide a clean API for configuring menu behavior and appearance
- Maintain consistency with existing Kotter patterns in the codebase

### Success Criteria
- Menu component can be instantiated with custom domain objects
- Supports unlimited nesting levels
- Configurable keyboard bindings and colors
- Can replace existing menu logic in PickGameScreen without losing functionality
- Well-tested with unit tests using Kotter's test support

## 2. Architecture and Design Decisions

### 2.1 Location
**Module**: `cli`
**Package**: `io.dungeons.cli.ui` (new package for reusable UI components)
**Files**:
- `cli/src/main/kotlin/cli/ui/MenuItem.kt` - Data model for menu items
- `cli/src/main/kotlin/cli/ui/MenuComponent.kt` - Main menu component logic
- `cli/src/main/kotlin/cli/ui/MenuConfig.kt` - Configuration options
- `cli/src/test/kotlin/cli/ui/MenuComponentTest.kt` - Unit tests

### 2.2 Design Patterns
- **Generic Type**: Menu is generic over type `T` to hold domain objects
- **Composition over Inheritance**: Menu is a component that can be embedded in screens, not a screen itself
- **Builder Pattern**: MenuConfig uses a builder-style API for configuration
- **Reactive State**: Uses Kotter's `LiveVar` and `LiveList` for state management
- **Callback Pattern**: Uses lambda callbacks for activation events

### 2.3 Integration with Screen Pattern
The MenuComponent will be used as a component within existing Screen implementations:
```kotlin
class MyScreen : Screen<ScreenTransition>(...) {
    private lateinit var menu: MenuComponent<MyDomainObject>

    override fun init(session: Session) {
        menu = MenuComponent.create(session) { ... }
    }

    override val sectionBlock: MainRenderScope.() -> Unit = {
        menu.render(this)
    }

    override val runBlock: RunScope.() -> Unit = {
        menu.handleInput(this)
    }
}
```

## 3. Data Model Design

### 3.1 MenuItem<T>

```kotlin
/**
 * Represents a single item in a hierarchical menu structure.
 *
 * @param T The type of domain object associated with this menu item
 * @property label The text to display for this menu item
 * @property domainObject Optional domain object associated with this item (null for structural nodes)
 * @property children Child menu items (empty for leaf nodes)
 */
data class MenuItem<T>(
    val label: String,
    val domainObject: T? = null,
    val children: List<MenuItem<T>> = emptyList(),
) {
    val isLeaf: Boolean get() = children.isEmpty()
    val hasChildren: Boolean get() = children.isNotEmpty()
}
```

**Design rationale**:
- Simple, immutable data structure
- Generic over T to support any domain type
- Separates structure (label, children) from domain data
- Computed properties for readability

### 3.2 MenuState<T>

```kotlin
/**
 * Internal state management for the menu component.
 *
 * @property currentNode The currently active menu node (whose children are displayed)
 * @property selectedIndex Index of the currently selected child item
 */
internal data class MenuState<T>(
    val currentNode: MenuItem<T>,
    val selectedIndex: Int = 0,
) {
    val currentChildren: List<MenuItem<T>> get() = currentNode.children
    val selectedItem: MenuItem<T>? get() = currentChildren.getOrNull(selectedIndex)
}
```

## 4. Component API Design

### 4.1 MenuConfig

```kotlin
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
```

### 4.2 MenuComponent<T>

```kotlin
/**
 * Reusable hierarchical menu component for Kotter-based CLI applications.
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

    /**
     * Renders the menu in the given MainRenderScope.
     * Call this from your screen's sectionBlock.
     */
    fun render(scope: MainRenderScope)

    /**
     * Handles keyboard input in the given RunScope.
     * Call this from your screen's runBlock.
     */
    fun handleInput(scope: RunScope)

    /**
     * Navigates to a child item (when it has children).
     */
    private fun navigateDown(item: MenuItem<T>)

    /**
     * Navigates back to parent level.
     * @return true if navigation occurred, false if already at root
     */
    fun navigateUp(): Boolean

    /**
     * Moves selection to previous item (wraps around).
     */
    private fun selectPrevious()

    /**
     * Moves selection to next item (wraps around).
     */
    private fun selectNext()

    /**
     * Activates the currently selected item.
     * - If item has children: navigate down
     * - If item is leaf: call onActivate callback
     */
    private fun activateSelected()

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
        ): MenuComponent<T>
    }
}
```

### 4.3 MenuBuilder<T> (DSL Helper)

```kotlin
/**
 * DSL builder for constructing menu hierarchies.
 */
class MenuBuilder<T> {
    /**
     * Creates a menu item with children.
     */
    fun branch(
        label: String,
        domainObject: T? = null,
        children: MenuBuilder<T>.() -> List<MenuItem<T>>,
    ): MenuItem<T>

    /**
     * Creates a leaf menu item (no children).
     */
    fun leaf(
        label: String,
        domainObject: T,
    ): MenuItem<T>
}
```

## 5. Implementation Steps

### Phase 1: Core Data Model (1 task)
- [ ] **Task 1.1**: Create `MenuItem.kt` data class
  - Location: `cli/src/main/kotlin/cli/ui/MenuItem.kt`
  - Define generic MenuItem with label, domainObject, children
  - Add computed properties: isLeaf, hasChildren
  - Write unit tests for MenuItem structure

### Phase 2: Configuration (1 task)
- [ ] **Task 2.1**: Create `MenuConfig.kt` configuration class
  - Location: `cli/src/main/kotlin/cli/ui/MenuConfig.kt`
  - Define all configuration options with defaults
  - Document each configuration option
  - Add validation if needed (e.g., prefixes shouldn't be too long)

### Phase 3: Core Menu Component (4 tasks)
- [ ] **Task 3.1**: Create `MenuComponent.kt` skeleton
  - Location: `cli/src/main/kotlin/cli/ui/MenuComponent.kt`
  - Define class structure with private constructor
  - Set up LiveVar and LiveList state management
  - Implement companion object factory method

- [ ] **Task 3.2**: Implement rendering logic
  - Implement `render()` method
  - Display breadcrumb if enabled
  - Display current menu items with selection indicator
  - Use config for prefix strings

- [ ] **Task 3.3**: Implement navigation logic
  - Implement `selectPrevious()` and `selectNext()` with bounds checking
  - Implement `navigateDown()` to push onto navigation stack
  - Implement `navigateUp()` to pop from navigation stack
  - Implement `activateSelected()` to dispatch to correct action

- [ ] **Task 3.4**: Implement input handling
  - Implement `handleInput()` method
  - Map configured keys to navigation actions
  - Handle all keys: up, down, activate, back

### Phase 4: DSL Builder (2 tasks)
- [ ] **Task 4.1**: Create `MenuBuilder.kt` DSL helper
  - Location: `cli/src/main/kotlin/cli/ui/MenuBuilder.kt`
  - Implement `branch()` and `leaf()` functions
  - Make DSL type-safe and easy to use
  - Add documentation examples

- [ ] **Task 4.2**: Integrate builder with MenuComponent.create()
  - Update companion object factory method
  - Accept MenuBuilder lambda
  - Validate root item has children

### Phase 5: Testing (3 tasks)
- [ ] **Task 5.1**: Write unit tests for MenuItem
  - Location: `cli/src/test/kotlin/cli/ui/MenuItemTest.kt`
  - Test structure creation
  - Test computed properties
  - Test edge cases (empty children, deep nesting)

- [ ] **Task 5.2**: Write unit tests for MenuComponent
  - Location: `cli/src/test/kotlin/cli/ui/MenuComponentTest.kt`
  - Use VirtualTerminal for testing
  - Test navigation (up, down, activate, back)
  - Test rendering output
  - Test callback invocation
  - Test bounds checking
  - Test configuration options

- [ ] **Task 5.3**: Write integration tests
  - Test with complex hierarchical structures
  - Test all configuration combinations
  - Test edge cases (single item, deep nesting, empty branches)

### Phase 6: Refactoring Existing Screens (2 tasks)
- [ ] **Task 6.1**: Refactor PickGameScreen to use MenuComponent
  - Location: `cli/src/main/kotlin/cli/screen/PickGameScreen.kt`
  - Replace MenuItem sealed class with MenuComponent
  - Replace manual navigation logic with MenuComponent
  - Keep existing functionality intact
  - Verify with existing tests (if any)

- [ ] **Task 6.2**: Refactor PickAdventureScreen to use MenuComponent
  - Location: `cli/src/main/kotlin/cli/screen/PickAdventureScreen.kt`
  - Same refactoring as PickGameScreen
  - Verify functionality

### Phase 7: Documentation and Examples (2 tasks)
- [ ] **Task 7.1**: Add KDoc documentation
  - Document all public APIs
  - Add usage examples in class-level KDoc
  - Document configuration options

- [ ] **Task 7.2**: Create example screen
  - Location: `cli/src/main/kotlin/cli/screen/MenuExampleScreen.kt`
  - Demonstrate all features of MenuComponent
  - Show different configurations
  - Can be used for manual testing

## 6. Testing Strategy

### 6.1 Unit Tests
Use Kotter's `kotter-test-support-jvm` library:
```kotlin
testImplementation("com.varabyte.kotterx:kotter-test-support-jvm")
```

**Test scenarios**:
1. MenuItem structure creation and properties
2. Navigation between levels (down into children, up to parent)
3. Selection wrapping (at bounds)
4. Activation callback invocation
5. Configuration options (custom keys, prefixes, breadcrumb)
6. Edge cases (empty menu, single item, deep nesting)
7. Rendering output verification

### 6.2 Integration Tests
Create a test screen with MenuComponent and verify:
1. Full navigation workflow
2. Interaction with Screen lifecycle
3. State persistence across re-renders
4. Multiple menu components in same screen

### 6.3 Manual Testing
Test with actual CLI application:
1. Run game and navigate menus
2. Verify visual appearance
3. Test with different terminal sizes
4. Verify keyboard responsiveness

## 7. Design Considerations and Trade-offs

### 7.1 State Management
**Decision**: Use navigation stack (LiveList) to track current path
**Rationale**:
- Allows easy back navigation
- Maintains history for breadcrumb
- Simpler than maintaining parent references

**Alternative considered**: Maintain parent reference in MenuItem
**Why rejected**: More complex, harder to serialize, circular references

### 7.2 Selection Wrapping
**Decision**: Allow wrapping at bounds (top/bottom)
**Rationale**:
- Better UX for small lists
- Consistent with common UI patterns
- Can be disabled via configuration if needed

**Alternative considered**: Stop at bounds
**Why rejected**: Less convenient for users, existing PickGameScreen doesn't wrap

**Update**: Check existing PickGameScreen behavior and match it

### 7.3 Generic Type Constraint
**Decision**: No constraint on T, nullable domainObject
**Rationale**:
- Maximum flexibility
- Branch nodes may not need domain objects
- Simpler type system

**Alternative considered**: Require T for all items
**Why rejected**: Forces unnecessary domain objects for structural branches

### 7.4 Separation of Concerns
**Decision**: MenuComponent handles rendering and input
**Rationale**:
- Encapsulates all menu logic
- Easy to embed in screens
- Reusable across different screens

**Alternative considered**: Separate renderer and controller
**Why rejected**: Over-engineering for current use case, harder to use

## 8. Future Enhancements

These are not part of the initial implementation but could be added later:

1. **Icons/Symbols**: Support for custom icons per item type
2. **Color Customization**: Allow configuring colors for selected/unselected items
3. **Search/Filter**: Quick search to filter items by label
4. **Multi-column Layout**: Display items in multiple columns for large lists
5. **Lazy Loading**: Load children on-demand for dynamic menus
6. **Animations**: Smooth transitions between levels (if Kotter supports it)
7. **Accessibility**: Screen reader support, high contrast mode
8. **Persistence**: Save/restore menu state across sessions
9. **Horizontal Navigation**: Support for horizontal menu layouts
10. **Context Actions**: Support for context menu (e.g., right-click actions)

## 9. Dependencies

### Existing Dependencies (already in build.gradle.kts)
- `com.varabyte.kotter:kotter-jvm:1.2.1`
- `com.varabyte.kotterx:kotter-test-support-jvm:1.2.1`

### No New Dependencies Required
The implementation uses only existing Kotter APIs and standard Kotlin library features.

## 10. Success Metrics

The implementation will be considered successful when:
1. All unit tests pass with >90% code coverage
2. PickGameScreen and PickAdventureScreen successfully refactored to use MenuComponent
3. No loss of functionality in refactored screens
4. Code duplication reduced (eliminate duplicate menu logic)
5. API is intuitive and well-documented
6. Example from concept document works exactly as specified

## 11. Example Usage

Based on the concept document, here's how the menu would be created:

```kotlin
val menu = MenuComponent.create(
    session = session,
    config = MenuConfig(
        selectedPrefix = ">",
        unselectedPrefix = " ",
        showBreadcrumb = true,
    ),
    onActivate = { action ->
        // Handle the selected action
        when (action) {
            is GameAction.GoTo -> handleGoTo(action.location)
            is GameAction.Open -> handleOpen(action.container)
            is GameAction.Investigate -> handleInvestigate(action.subject)
        }
    },
) {
    branch("What are you going to do?") {
        listOf(
            branch("Go To") {
                listOf(
                    leaf("The northern door", GameAction.GoTo("northern door")),
                    leaf("The western hallway", GameAction.GoTo("western hallway")),
                )
            },
            branch("Open") {
                listOf(
                    leaf("The trunk", GameAction.Open("trunk")),
                )
            },
            branch("Investigate") {
                listOf(
                    leaf("The strange noise", GameAction.Investigate("strange noise")),
                    leaf("The dark passage", GameAction.Investigate("dark passage")),
                )
            },
        )
    }
}
```

## 12. Risk Assessment

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| Kotter API limitations | Medium | Medium | Study Kotter docs thoroughly before implementation |
| Performance with deep nesting | Low | Low | Test with realistic menu depths, add depth limit if needed |
| State synchronization issues | Medium | High | Use Kotter's reactive primitives correctly, test thoroughly |
| Breaking existing screens | Low | High | Keep old code until refactoring tested, feature flag if needed |
| Keyboard event conflicts | Low | Medium | Make keys configurable, document conflicts |

## 13. Timeline Estimate

This is an estimate for planning purposes:

- Phase 1 (Data Model): 0.5 day
- Phase 2 (Configuration): 0.25 day
- Phase 3 (Core Component): 2 days
- Phase 4 (DSL Builder): 0.5 day
- Phase 5 (Testing): 1.5 days
- Phase 6 (Refactoring): 1 day
- Phase 7 (Documentation): 0.5 day

**Total: ~6-7 days** (with testing and documentation)

Note: This estimate assumes working solo and includes time for learning Kotter APIs, writing tests, and documentation.

## 14. Implementation Notes

- Follow existing code style in the codebase (detekt rules)
- Run `./gradlew detekt` after each phase
- Use meaningful commit messages referencing tasks
- Keep PRs focused on single phases for easier review
- Update this document if design decisions change during implementation
- Consult `docs/decisions.md` for architectural decision recording
- Follow unit testing guidelines from `docs/unit_tests.md`

## 15. Open Questions

Before starting implementation, resolve:

1. Should selection wrap at bounds, or stop? (Check PickGameScreen behavior)
2. What should happen when activating a branch with empty children?
3. Should we support dynamic menu updates (LiveList of items)?
4. Do we need thread safety for the navigation stack?
5. Should breadcrumb be clickable for quick navigation?
6. What's the max reasonable depth for menu nesting?

These questions should be answered during Phase 1-2 or by consulting with stakeholders.
