# Security Review Branch - Comprehensive Code Review
**Date**: 2025-12-09
**Branch**: security-review
**Base**: main
**Reviewers**: Kotlin Idioms Expert, Peer Reviewer, Software Architect

## Executive Summary

This review analyzed 55 changed files with 1,342 insertions and 21 deletions. The changes introduce a multi-module architecture with three new modules (`cli`, `tool`, `persistence`) alongside existing `app` and `dungeons-domain` modules. While the separation of concerns is well-intentioned, the implementation introduces **39+ issues** ranging from critical security vulnerabilities to architectural violations that will impact long-term maintainability.

### Issue Breakdown
- **Critical Security Issues**: 3
- **Critical Architectural Issues**: 5
- **Critical Bugs**: 3
- **High Priority Issues**: 13
- **Medium/Low Priority Issues**: 15+

---

## 🔴 Critical Issues (Must Fix Before Production)

### Security Vulnerabilities

#### 1. Hardcoded Credentials in Version Control
**Severity**: Critical
**Files**:
- `docker-compose.yml:8-9`
- `tool/src/main/resources/application.properties:10-11`

**Problem**:
```yaml
# docker-compose.yml
MONGO_INITDB_ROOT_USERNAME: admin
MONGO_INITDB_ROOT_PASSWORD: devpassword
```

```properties
# application.properties
spring.mongodb.username=dev
spring.mongodb.password=dev
```

**Risk**: Credentials exposed if repository becomes public or accessed by unauthorized users. Even "dev" credentials in version control create security risks.

**Fix**:
```yaml
# docker-compose.yml
MONGO_INITDB_ROOT_USERNAME: ${MONGO_ROOT_USERNAME:-admin}
MONGO_INITDB_ROOT_PASSWORD: ${MONGO_ROOT_PASSWORD}
```

```properties
# application.properties
spring.mongodb.username=${MONGODB_USERNAME}
spring.mongodb.password=${MONGODB_PASSWORD}
```

Store actual credentials in `.env` file (already in .gitignore) and document required environment variables in README.

---

#### 2. Information Disclosure via Logging
**Severity**: Critical
**File**: `app/src/main/kotlin/app/rest/WorldController.kt:16`

**Problem**:
```kotlin
logger.info("User ${user.username} was provided")
```

**Risk**:
- Username logging may violate privacy requirements
- String interpolation vulnerable to log injection if username contains newlines/special characters
- Sensitive information exposure in logs

**Fix**:
```kotlin
logger.debug("User authenticated: {}", user.username)  // Use DEBUG level
// OR sanitize before logging
logger.info("User authenticated: {}", sanitizeForLogging(user.username))
```

---

#### 3. Hardcoded Dev Token
**Severity**: Medium (but Critical if dev profile used in production)
**File**: `app/src/main/kotlin/app/security/DevTokenAuthenticationFilter.kt:179`

**Problem**:
```kotlin
private const val DEV_TOKEN = "this-is-our-dev-token"
```

**Risk**: Simple, predictable token. If dev profile accidentally enabled in production, creates security hole.

**Fix**:
- Generate random token on startup and log it
- Or require via environment variable
- Add explicit checks to prevent dev profile in production

---

### Critical Architectural Violations

#### 4. Domain Layer Contaminated with Spring Framework
**Severity**: Critical
**Files**:
- `dungeons-domain/src/main/kotlin/domain/savegame/NewGameUseCase.kt`
- `dungeons-domain/src/main/kotlin/domain/narrator/NarrateRoomQuery.kt`
- `dungeons-domain/src/main/kotlin/domain/adventure/ListAdventuresQuery.kt`
- `dungeons-domain/src/main/kotlin/domain/savegame/MockSaveGameRepository.kt`

**Problem**:
```kotlin
@Component  // ❌ Spring annotation in domain
class NewGameUseCase(private val saveGameRepository: SaveGameRepository) {
    // Domain logic shouldn't know about Spring
}
```

**Why This Violates Clean Architecture**:
1. **Coupling**: Domain now depends on Spring framework, can't be reused without Spring
2. **Testing**: Forces Spring context for unit tests (slow, complex)
3. **Portability**: Cannot use domain logic in non-Spring environments (Kotlin/Native, Kotlin/JS, Android)
4. **Dependency Inversion**: Infrastructure should depend on domain, not vice versa

**Proper Dependency Flow**:
```
Infrastructure → Application → Domain
(Spring)      → (Use Cases)  → (Pure Kotlin)
```

**Current Problematic Flow**:
```
Domain → Spring Framework ❌
```

**Fix**:
```kotlin
// ✅ Domain remains pure (no annotations)
class NewGameUseCase(private val saveGameRepository: SaveGameRepository) {
    fun execute(userId: UUID, adventure: Adventure): Id<SaveGame> {
        // Pure domain logic
    }
}

// ✅ Configuration in application modules (app, cli, tool)
@Configuration
class DomainConfiguration {
    @Bean
    fun newGameUseCase(repository: SaveGameRepository) = NewGameUseCase(repository)

    @Bean
    fun narrateRoomQuery(
        saveGameRepository: SaveGameRepository,
        adventureRepository: AdventureRepository,
        roomRepository: RoomRepository
    ) = NarrateRoomQuery(saveGameRepository, adventureRepository, roomRepository)
}
```

**Action Items**:
- [ ] Remove ALL Spring annotations from `dungeons-domain` module
- [ ] Remove Spring dependency from `dungeons-domain/build.gradle.kts`
- [ ] Create `@Configuration` classes in app/cli/tool modules
- [ ] Verify domain tests run without Spring context

---

#### 5. Spring Boot Plugin Applied to Library Modules
**Severity**: Critical
**File**: `buildSrc/src/main/kotlin/buildlogic.kotlin-library-conventions.gradle.kts`

**Problem**:
```kotlin
plugins {
    id("buildlogic.kotlin-common-conventions")
    id("org.springframework.boot")  // ❌ Libraries shouldn't be Spring Boot apps!
    `java-library`
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false  // ⚠️ Workaround indicating architectural mistake
}
```

**Issues**:
1. **Wrong Abstraction**: Libraries don't need Spring Boot plugin
2. **Workaround Culture**: Disabling `bootJar` is a code smell
3. **Build Complexity**: Unnecessary plugin adds overhead
4. **Confusion**: Spring Boot applied but disabled creates cognitive overhead

**Fix**:
```kotlin
// buildlogic.kotlin-library-conventions.gradle.kts
plugins {
    id("buildlogic.kotlin-common-conventions")
    id("io.spring.dependency-management")  // ✅ For Spring dependencies only
    `java-library`
}

// Remove bootJar workaround entirely
```

Then in modules that need Spring dependencies:
```kotlin
// persistence/build.gradle.kts
plugins {
    id("buildlogic.kotlin-library-conventions")
    kotlin("plugin.spring")  // For Spring annotations only
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.0")
    }
}
```

---

#### 6. Multiple Spring Boot Applications with Overlapping Component Scans
**Severity**: Critical
**Files**:
- `cli/src/main/kotlin/cli/KotterCli.kt`
- `tool/src/main/kotlin/tool/ToolApplication.kt`

**Problem**:
```kotlin
// Both apps do this:
@SpringBootApplication(scanBasePackages = ["io.dungeons"])
```

**Issues**:
1. **Over-scanning**: Scans ALL packages, loads components from other modules unintentionally
2. **Namespace Collision**: Both apps load domain components, persistence config, mock repositories
3. **Startup Overhead**: Unnecessary component scanning slows boot time
4. **Hidden Dependencies**: Not clear which beans each app actually needs
5. **Resource Conflicts**: Both try to connect to MongoDB, load same configurations

**Example Collision**:
- Both apps load `MockSaveGameRepository` from domain
- Both load `PersistenceConfig` from persistence
- Both connect to MongoDB (even if not needed)

**Fix Option 1 - Specific Scanning**:
```kotlin
@SpringBootApplication(scanBasePackages = [
    "io.dungeons.cli",                      // Own components
    "io.dungeons.domain.savegame",          // Specific use cases
    "io.dungeons.persistence.mongodb"       // Infrastructure
])
```

**Fix Option 2 - Explicit Configuration (Recommended)**:
```kotlin
@SpringBootApplication
@Import(
    PersistenceConfig::class,
    DomainConfiguration::class,
    CliConfiguration::class
)
class KotterCli
```

---

#### 7. Repository Pattern Leaks MongoDB Implementation Details
**Severity**: High
**File**: `persistence/src/main/kotlin/persistence/mongodb/MongoDBAdventureRepository.kt`

**Problem**:
```kotlin
@Repository
interface MongoDBAdventureRepository :
    AdventureRepository,           // Domain interface ✅
    MongoRepository<...>,          // Spring Data interface ❌
    CrudRepository<...>            // Spring Data interface ❌ (redundant!)
```

**Issues**:
1. **Interface Segregation Violation**: Domain interface now coupled to MongoDB types
2. **Redundancy**: `MongoRepository` already extends `CrudRepository`
3. **Leaky Abstraction**: MongoDB-specific methods (insert, findAll(Pageable), etc.) leak into domain

**Impact**:
Domain defines minimal interface, but consumers now get dozens of MongoDB methods through inheritance chain.

**Fix - Use Composition Over Inheritance**:
```kotlin
// ✅ Implementation class delegates to Spring Data
@Repository
class MongoDBAdventureRepository(
    private val springDataRepository: MongoAdventureSpringDataRepository
) : AdventureRepository {
    override fun findById(id: Id<Adventure>): Adventure? =
        springDataRepository.findById(id).orElse(null)

    override fun findAll(): List<Adventure> =
        springDataRepository.findAll()

    override fun save(entity: Adventure): Adventure =
        springDataRepository.save(entity)
}

// Internal Spring Data interface, not exposed to domain
internal interface MongoAdventureSpringDataRepository :
    MongoRepository<Adventure, Id<Adventure>>
```

---

#### 8. CLI Module Depends on Persistence Directly
**Severity**: High
**File**: `cli/build.gradle.kts`

**Problem**:
```kotlin
dependencies {
    implementation(project(":dungeons-domain"))
    implementation(project(":persistence"))  // ❌ Direct infrastructure dependency
```

**Issues**:
1. **Layer Violation**: Application layer shouldn't depend on specific infrastructure
2. **Coupling**: CLI now coupled to MongoDB; can't switch to PostgreSQL or in-memory
3. **Testing Difficulty**: Can't test CLI without MongoDB

**Better Architecture**:
```
cli → dungeons-domain (interfaces only)
persistence → dungeons-domain (implements interfaces)
Runtime wiring via Spring configuration
```

**Fix**:
CLI should only depend on `dungeons-domain`. At runtime, Spring injects the implementation:

```kotlin
// cli/build.gradle.kts
dependencies {
    implementation(project(":dungeons-domain"))
    // Remove: implementation(project(":persistence"))

    // Only for runtime wiring
    runtimeOnly(project(":persistence"))
}
```

---

### Critical Bugs

#### 9. Race Condition in GameStateHolder
**Severity**: High
**File**: `cli/src/main/kotlin/cli/GameState.kt:17-21`

**Problem**:
```kotlin
class GameStateHolder {
    private var _gameState: AtomicReference<GameState> = AtomicReference(GameState())

    var gameState: GameState
        get() = _gameState.get()
        set(value) {
            _gameState.set(value)
        }
```

**Bug**: The getter/setter pattern provides NO atomicity guarantees. Between `get()` and `set()`, another thread can modify the value, causing lost updates.

**Specific Issue** (`PickAdventureScreen.kt:765-768`):
```kotlin
val newGameState = gameStateHolder.gameState.copy(  // Read
    currentGameId = gameId,
)
gameStateHolder.gameState = newGameState  // Write - NOT ATOMIC!
```

**Fix**:
```kotlin
@Component
class GameStateHolder {
    private val _gameState = AtomicReference(GameState())

    val gameState: GameState
        get() = _gameState.get()

    fun updateGameState(updater: (GameState) -> GameState): GameState =
        _gameState.updateAndGet(updater)
}

// Usage:
gameStateHolder.updateGameState { it.copy(currentGameId = gameId) }
```

Or simpler for single-threaded CLI:
```kotlin
@Component
class GameStateHolder {
    var gameState: GameState = GameState()
        private set

    fun updateGameState(updater: (GameState) -> GameState) {
        gameState = updater(gameState)
    }
}
```

---

#### 10. Authorization Bypass in MockSaveGameRepository
**Severity**: High
**File**: `dungeons-domain/src/main/kotlin/domain/savegame/MockSaveGameRepository.kt:16`

**Problem**:
```kotlin
override fun findByUserId(userId: Id<User>, saveGameId: Id<SaveGame>): SaveGame? =
    storage[userId]  // ❌ Ignores saveGameId parameter!
```

**Bug**: The `saveGameId` parameter is completely ignored. This means:
1. Multiple save games per user not supported
2. User could access another user's save game by providing their userId
3. Repository only stores one save game per user

**Security Impact**: Potential authorization bypass - users could access other users' game data.

**Fix**:
```kotlin
@Component
@Profile("!prod")  // Never in production
class MockSaveGameRepository : SaveGameRepository {
    private val storage: MutableMap<Pair<Id<User>, Id<SaveGame>>, SaveGame> =
        ConcurrentHashMap()

    override fun save(saveGame: SaveGame) {
        storage[Pair(saveGame.userId, saveGame.id)] = saveGame
    }

    override fun findByUserId(userId: Id<User>, saveGameId: Id<SaveGame>): SaveGame? =
        storage[Pair(userId, saveGameId)]
}
```

---

#### 11. Mutable Data Class with Suppressed Warnings
**Severity**: High
**File**: `cli/src/main/kotlin/cli/GameState.kt:11`

**Problem**:
```kotlin
@Suppress("DataClassContainsFunctions", "DataClassShouldBeImmutable")
data class GameState(var player: Player? = null, var currentGameId: Id<SaveGame>? = null)
```

**Issues**:
1. **Suppressing Warnings**: Defeats purpose of linter, indicates design problem
2. **Mutability**: `var` properties make equals/hashCode/copy unreliable
3. **Thread Safety**: Combined with AtomicReference, creates race conditions
4. **Data Class Misuse**: Data classes should be immutable value objects

**Fix**:
```kotlin
// ✅ Immutable data class
data class GameState(
    val player: Player? = null,
    val currentGameId: Id<SaveGame>? = null
)

// Update by replacing entire state
gameStateHolder.gameState = gameState.copy(player = newPlayer)
```

---

## 🟡 High Priority Issues

### Kotlin Idiom Violations

#### 12. Unnecessary AtomicReference Wrapper
**File**: `cli/src/main/kotlin/cli/GameState.kt:14-21`

**Problem**: `AtomicReference` is a Java concurrency primitive rarely needed in Kotlin. CLI is single-threaded, doesn't need atomicity.

**Fix**: Use simple property or `@Volatile` if thread safety needed:
```kotlin
@Component
class GameStateHolder {
    @Volatile
    var gameState: GameState = GameState()
        private set
}
```

---

#### 13. Using Java Optional Instead of Kotlin Nullable
**File**: `domain/adventure/MockAdventureRepository.kt:30`

**Problem**:
```kotlin
override fun findById(id: Id<Adventure>): Optional<Adventure> =
    Optional.ofNullable(adventures.find { it.id == id })
```

Mixing Java `Optional` with Kotlin nullable types is anti-pattern.

**Fix**: Update interface to use Kotlin nullable:
```kotlin
interface AdventureRepository {
    fun findById(id: Id<Adventure>): Adventure?  // ✅ Kotlin way
}

override fun findById(id: Id<Adventure>): Adventure? =
    adventures.find { it.id == id }
```

If you must keep `Optional` for Java interop, current implementation is acceptable but not idiomatic.

---

#### 14. Not Using requireNotNull() Idiomatically
**Files**: Multiple (e.g., `domain/narrator/NarrateRoomQuery.kt:18-22`)

**Problem**:
```kotlin
val saveGame = saveGameRepository.findByUserId(userId, saveGameId)
require(saveGame != null) { "No save game found for user $userId" }
// Now need explicit cast or smart cast
```

**Fix**:
```kotlin
val saveGame = requireNotNull(saveGameRepository.findByUserId(userId, saveGameId)) {
    "No save game found for user $userId"
}
// Smart cast to non-null automatically
```

---

#### 15. Using count() on List Instead of size
**File**: `tool/src/main/kotlin/tool/commands/HelloCommand.kt:29,31`

**Problem**:
```kotlin
echo("You have ${adventures.count()} adventures")
if(adventures.count() == 0) {  // Also: missing space after 'if'
```

`count()` iterates entire collection (O(n)). For `List`, use `.size` property (O(1)).

**Fix**:
```kotlin
echo("You have ${adventures.size} adventures")
if (adventures.isEmpty()) {  // Most idiomatic
```

---

#### 16. Explicit .Companion Usage
**File**: `tool/src/main/kotlin/tool/commands/GenIdCommand.kt:23`

**Problem**:
```kotlin
val newId = Id.Companion.generate<String>()
```

`.Companion` is implicit in Kotlin.

**Fix**:
```kotlin
val newId = Id.generate<String>()
```

---

#### 17. String Concatenation Instead of String Templates
**File**: `app/src/main/kotlin/app/config/ProfileValidator.kt:17-18`

**Problem**:
```kotlin
"No environment profile active. Please activate exactly one of: " +
    "${requiredProfiles.joinToString(", ")}.",
```

**Fix**:
```kotlin
"No environment profile active. Please activate exactly one of: ${requiredProfiles.joinToString(", ")}."
```

---

### Code Smells & Design Issues

#### 18. Mock Repository in Production Code
**File**: `dungeons-domain/src/main/kotlin/domain/savegame/MockSaveGameRepository.kt`

**Problem**:
- Mock implementation in `src/main` with `@Component` annotation
- Can accidentally be loaded in production if profiles misconfigured
- Violates production code hygiene

**Fix**:
- Move to `src/test/kotlin`
- Or create separate `test-infrastructure` module
- Use `@Profile("!prod")` to prevent production loading
- Better: use `@TestConfiguration` and keep out of main sources

---

#### 19. Empty runBlock in RoomScreen
**File**: `cli/src/main/kotlin/cli/screen/RoomScreen.kt:812-813`

**Problem**:
```kotlin
override val runBlock: RunScope.() -> Unit
    get() = {}
```

Screen has no user interaction logic. Will hang with no way to exit.

**Fix**: Add minimum interaction handling:
```kotlin
override val runBlock: RunScope.() -> Unit
    get() = {
        onKeyPressed {
            when (key) {
                Keys.ESC -> exit(this, ScreenTransition.Exit)
                // Add game commands
            }
        }
    }
```

---

#### 20. Potential IndexOutOfBoundsException
**File**: `cli/src/main/kotlin/cli/screen/PickAdventureScreen.kt:755-756`

**Problem**:
```kotlin
override fun init(session: Session) {
    adventures = session.liveListOf(listAdventuresQuery.execute())
    selectedAvdenture = session.liveVarOf(0)  // ❌ Crashes if list empty
}
```

**Fix**:
```kotlin
override fun init(session: Session) {
    val adventureList = listAdventuresQuery.execute()
    require(adventureList.isNotEmpty()) { "No adventures available" }
    adventures = session.liveListOf(adventureList)
    selectedAdventure = session.liveVarOf(0)  // Also: fix typo
}
```

---

#### 21. Generic Exception Catching with Suppressed Warning
**File**: `app/src/main/kotlin/app/security/DevTokenAuthenticationFilter.kt:162`

**Problem**:
```kotlin
@Suppress("TooGenericExceptionCaught")
try {
    val userDetails = userDetailsService.loadUserByUsername(DEFAULT_USERNAME)
    // ...
} catch (e: Exception) {
    logger.error("Error during dev token authentication", e)
}
```

Catching all exceptions and logging means authentication failures are silently ignored.

**Fix**:
```kotlin
try {
    val userDetails = userDetailsService.loadUserByUsername(DEFAULT_USERNAME)
    // ...
} catch (e: UsernameNotFoundException) {
    logger.error("Dev user not found: $DEFAULT_USERNAME", e)
    // Consider: set error response or rethrow
} catch (e: AuthenticationException) {
    logger.error("Authentication failed for dev token", e)
}
```

---

#### 22. Inconsistent Logging Implementation
**Files**: Multiple

**Problem**: Mixed logging approaches:
```kotlin
// Some files use SLF4J (correct):
private val logger = LoggerFactory.getLogger(WorldController::class.java)

// Others use java.util.logging:
private val logger = getLogger(NewGameUseCase::class.java.name)
```

**Fix**: Standardize on SLF4J throughout the codebase.

---

#### 23. Missing Validation in NarrateRoomQuery
**File**: `dungeons-domain/src/main/kotlin/domain/narrator/NarrateRoomQuery.kt:1117-1122`

**Problem**:
```kotlin
val saveGame = saveGameRepository.findByUserId(userId, saveGameId)
require(saveGame != null) { "No save game found for user $userId" }
// ❌ No verification that saveGame.userId == userId
```

Combined with bug #10, could allow unauthorized access.

**Fix**:
```kotlin
val saveGame = requireNotNull(saveGameRepository.findByUserId(userId, saveGameId)) {
    "No save game found for user $userId"
}
require(saveGame.userId == userId) {
    "SaveGame $saveGameId does not belong to user $userId"
}
```

---

#### 24. No Profile Validation in CLI/Tool Modules
**File**: Only `app` module has `ProfileValidator`

**Problem**: CLI and tool modules don't validate Spring profiles, could run with incorrect configuration.

**Fix**:
- Extract profile validation to shared module
- Apply to all Spring Boot applications
- Ensure mock repositories never load in production

---

## 🟢 Medium/Low Priority Issues

### Code Quality

#### 25. Variable Name Typo
**File**: `cli/src/main/kotlin/cli/screen/PickAdventureScreen.kt:33`

**Problem**: `selectedAvdenture` should be `selectedAdventure`

---

#### 26. Magic Number
**File**: `cli/src/main/kotlin/cli/screen/MyScreen.kt:38`

**Problem**: Hardcoded `5` for history size

**Fix**: Extract constant:
```kotlin
private const val MAX_HISTORY_SIZE = 5
```

---

#### 27. Unnecessary Semicolons
**File**: `tool/src/main/kotlin/tool/commands/CreateAdventureCommand.kt:53`

**Problem**: Java-style semicolon in Kotlin code

**Fix**: Remove semicolon

---

#### 28. Dead/Commented Code
**Files**:
- `cli/src/main/kotlin/cli/CliApplication.kt` (commented annotations, unused main)
- Multiple `build.gradle.kts` files (commented dependencies)

**Problem**: Commented code creates confusion, should be removed (it's in git history)

---

#### 29. Unnecessary Intermediate Variables
**File**: `cli/src/main/kotlin/cli/KotterCli.kt:45-50`

**Problem**:
```kotlin
private fun login(gameStateHolder: GameStateHolder) {
    val gameState = gameStateHolder.gameState  // Unnecessary
    gameStateHolder.gameState = gameState.copy(
        player = Player(Id.generate()),
    )
}
```

**Fix**:
```kotlin
private fun login(gameStateHolder: GameStateHolder) {
    gameStateHolder.gameState = gameStateHolder.gameState.copy(
        player = Player(Id.generate())
    )
}
```

Or as extension:
```kotlin
fun GameStateHolder.login() {
    gameState = gameState.copy(player = Player(Id.generate()))
}
```

---

#### 30. Unnecessary Type Annotations
**File**: `cli/src/main/kotlin/cli/screen/PickAdventureScreen.kt:79`

**Problem**:
```kotlin
val userId: UUID = player.id.toUUID()  // Type annotation redundant
```

**Fix**:
```kotlin
val userId = player.id.toUUID()
```

---

#### 31. Unnecessary Explicit this
**File**: `cli/src/main/kotlin/cli/screen/MyScreen.kt:38-42`

**Problem**: Using `this` explicitly in extension function

**Fix**: Remove `this`, it's implicit

---

#### 32. Missing else Branch in when
**File**: `cli/src/main/kotlin/cli/screen/PickAdventureScreen.kt:47-67`

**Problem**: No else branch for unhandled keys

**Fix**: Document unhandled keys or add logging

---

#### 33. Inconsistent Spacing
**File**: `tool/src/main/kotlin/tool/commands/HelloCommand.kt:31`

**Problem**: Missing space after `if`

---

#### 34. Unused TODO
**File**: `domain/adventure/MockAdventureRepository.kt:1031`

**Problem**:
```kotlin
override fun save(entity: Adventure): Adventure? {
    TODO("Not yet implemented")
}
```

Will crash if called.

**Fix**: Implement or throw `UnsupportedOperationException` with clear message

---

#### 35. Missing Newline at End of Files
**Files**: Multiple (buildlogic.kotlin-library-conventions.gradle.kts, docker-compose.yml, etc.)

**Problem**: POSIX standard violation, can cause git issues

---

### Architectural Improvements

#### 36. Hardcoded Screen Transitions
**File**: `cli/src/main/kotlin/cli/screen/Screen.kt`

**Problem**: Enum-based transitions prevent dynamic screen additions

**Recommendation**: Use registry pattern for extensibility

---

#### 37. Inefficient Entity Design
**File**: `dungeons-domain/src/main/kotlin/domain/adventure/Adventure.kt`

**Problem**: Adventure embeds entire rooms collection

**Impact**:
- Large documents in MongoDB
- Expensive aggregation queries (see `MongoDBRoomRepository`)
- Update anomalies

**Recommendation**: Store rooms separately, reference by ID

---

#### 38. Duplicate Player/User Concept
**Files**:
- `cli/GameState.kt` uses `Player`
- Domain uses `User`

**Problem**: Inconsistent terminology, requires conversions

**Recommendation**: Pick one term (probably `Player` for D&D context)

---

#### 39. Log File Naming Inconsistency
**File**: `tool/src/main/resources/logback-spring.xml:1793`

**Problem**: Tool's logs named `cli-application.%d.log` instead of `tool.%d.log`

---

## 📋 Action Plan

### Phase 1: Critical Security & Bugs (Immediate - Before Merge)
- [ ] Remove hardcoded credentials from docker-compose.yml and application.properties
- [ ] Fix information disclosure in WorldController logging
- [ ] Fix authorization bypass in MockSaveGameRepository
- [ ] Fix race condition in GameStateHolder (make GameState immutable)
- [ ] Fix typo: selectedAvdenture → selectedAdventure
- [ ] Add validation in NarrateRoomQuery

**Estimated Effort**: 4-6 hours

---

### Phase 2: Critical Architecture (Next Sprint - 1-2 weeks)
- [ ] Remove ALL Spring annotations from dungeons-domain module
- [ ] Remove Spring dependency from dungeons-domain/build.gradle.kts
- [ ] Create @Configuration classes in app/cli/tool for bean wiring
- [ ] Fix build conventions (remove Spring Boot from library plugin)
- [ ] Fix repository pattern with composition over inheritance
- [ ] Fix component scanning (use specific packages or @Import)

**Estimated Effort**: 2-3 days

---

### Phase 3: High Priority Code Quality (Sprint +1 - 1 week)
- [ ] Move MockSaveGameRepository to test sources or use @Profile("!prod")
- [ ] Standardize on SLF4J logging throughout
- [ ] Fix all requireNotNull() usage
- [ ] Replace Optional<T> with T? in repositories
- [ ] Remove AtomicReference wrapper
- [ ] Fix empty runBlock in RoomScreen
- [ ] Add IndexOutOfBounds protection in PickAdventureScreen
- [ ] Improve error handling in DevTokenAuthenticationFilter

**Estimated Effort**: 2-3 days

---

### Phase 4: Technical Debt & Cleanup (Backlog)
- [ ] Refactor module dependencies (CLI shouldn't depend directly on persistence)
- [ ] Redesign entity model (Adventure should reference rooms, not embed)
- [ ] Extract profile validation to shared module
- [ ] Remove dead/commented code
- [ ] Fix all minor Kotlin idiom violations
- [ ] Consolidate Spring Boot applications or separate projects
- [ ] Implement screen registry for extensibility

**Estimated Effort**: 1-2 weeks

---

## 🎯 Priority Recommendations

If you can only fix **5 things** before merging:

1. **Remove hardcoded credentials** - Critical security issue
2. **Remove Spring from domain** - Prevents future architectural pain
3. **Fix MockSaveGameRepository authorization** - Security vulnerability
4. **Make GameState immutable** - Prevents race conditions
5. **Fix build conventions** - Prevents confusion and build issues

---

## 📊 Metrics

### Code Health
- Lines Changed: 1,342 additions, 21 deletions
- Files Changed: 55
- New Modules: 3 (cli, tool, persistence)
- Critical Issues: 11
- High Priority Issues: 13
- Medium/Low Issues: 15+

### Architecture
- Layer Violations: 5 identified
- Dependency Inversions: 3 identified
- Code Smells: 10+ identified

### Maintainability Impact
- **Current**: Medium-High Risk
- **After Phase 1-2 Fixes**: Low-Medium Risk
- **After All Phases**: Low Risk

---

## 👍 Positive Observations

Despite the issues, there are good architectural decisions:

1. ✅ Module separation attempt shows good architectural thinking
2. ✅ Repository pattern used (though implementation needs fixes)
3. ✅ Use of data classes for DTOs
4. ✅ Proper use of when expressions
5. ✅ Extension functions used appropriately in places
6. ✅ Good use of Kotlin null safety in many places
7. ✅ Companion objects correctly placed at end of classes
8. ✅ Good attempt at domain-driven design

The foundation is solid; it needs refinement in execution.

---

## 📚 References

- [Clean Architecture by Robert Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Spring Boot Best Practices](https://spring.io/guides)
- [Repository Pattern](https://martinfowler.com/eaaCatalog/repository.html)

---

**Review Completed**: 2025-12-09
**Reviewers**: Multi-agent analysis (Kotlin Expert, Security Analyst, Architect)
**Next Review**: After Phase 1 fixes implemented
