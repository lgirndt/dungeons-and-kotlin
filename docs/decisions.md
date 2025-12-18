# Decisions

We are listing all architectural and significant technical decisions made on this project. 

# Hexagonal Architecture
- We need to separate the core business logic from external systems and frameworks.
- We will implement Hexagonal Architecture (also known as Ports and Adapters) to achieve this separation.
- Driver adapters are implemented as follows:
    - Access to Drivers are provided by Repository interfaces in the core domain.
    - The implementation of the Driver is done in the infrastructure layer.
- Driving adapters are implemented as follows:
    - We use Command Query Responsibility Segregation (CQRS) pattern.
    - Commands are realized as XyUseCase in the domain layer.
    - Queries are realized as XyQuery in the domain layer and they expose either Domain Objects or
      DTOs to the application layer.

# Domain Layer
- We use Spring intentionally in the domain layer to annotate Services. We want to keep the effort of
  wiring the application together as low as possible.
- We use domain objects also as our entities in the persistence layer to avoid mapping overhead.
- We do not annotate domain objects with spring data annotations. Neither @Id, @Document or @MongoId 
  annotations are used in the domain layer.
- Instead we make sure that our domain objects comply the to the spring conventions for mapping objects
  to MongoDB documents. For example, we make sure that every domain object has a field named "id" of type
  UUID.
- Entity IDs
    - We strongly type entity ids. Therefore we defined io.dungeons.port.Id which is defined in
      app-port/src/main/kotlin/port/Id.kt. This is a value class wrapping a UUID.
    - We have a challenge to solve: We want to use the ID in the domain layer, but we also want to
      provide in the client layers, that potentially don't depend on the domain layer.
    - Therefore we've decided to define alias classes for each Domain Object Id. For instance, 
      there is a domain class Player in the domain layer. In app-port/src/main/kotlin/port/Id.kt we
      define
```kotlin
@JvmInline
value class Id<T>(val value: UUID) {
//...
}

@Suppress("ClassNaming")
object _Player

typealias PlayerId = Id<_Player>
```
    - This way we can use PlayerId in the domain layer as well as in the client layers without
      introducing a dependency from the client layers to the domain layer.
    - We may only use the typealias in all modules, we shall never use Id<_AnyType> directly.
    - We will define an marker Type _SomeType for every Domain object that needs to have an ID.
    - In case of using the Companion methods with type parameters, we need to make sure that we
      never expose the marker type like `Id.generate<_Player>()`. Instead we need to solve this
      with type inference like `val id : PlayerId = Id.generate()`

# Gradle Multi Module Project
- We realize the hexagonal architecture using a Gradle multi module project to clearly separate the different 
  layers and their dependencies.

# Spring Boot
- Since we are using a multimodule project, every module needs to be a spring boot gradle project, otherwise
  we could not use spring boot dependencies in these modules.
- We are implementing Repositories with Spring Data, especially Spring Data MongoDB. As a consequence, query
  methods will return a Java Optional<T> instead of Kotlin nullable types. We need to accept this.

# Test Data Organization
- Test data prototype instances (SOME_X pattern) are organized in a structured, maintainable way.
- Each SOME_X instance is defined in the test scope of the module where class X is defined.
- Test data instances are placed in `TestDataInstances.kt` files within the same package as their corresponding domain object.
- This ensures test data lives close to the code it supports while remaining in the test scope.
- Modules that need test data from other modules (e.g., persistence tests using domain test data) include the test package as a dependency via Gradle's `testClasses` configuration.
- This approach maintains DRY principles while respecting module boundaries and avoiding circular dependencies.

# Logging with KotlinLogging
- We use the kotlin-logging library (io.github.oshai:kotlin-logging-jvm) for all logging in the project.
- Logger declaration: Use top-level private logger declaration instead of companion objects or class properties.
  ```kotlin
  private val logger = KotlinLogging.logger {}
  ```
- This approach is preferred over companion objects as they are heavyweight constructs in Kotlin.
- Lazy evaluation: Always use lambda syntax for log messages to ensure they are only constructed when the log level is enabled.
  ```kotlin
  logger.info { "Processing user: ${user.name}" }
  logger.error(exception) { "Failed to register player '${name}'" }
  ```
- The lambda syntax provides better performance by avoiding string construction when logging is disabled.
- For error logging with exceptions, pass the exception as the first parameter followed by the lambda message.
- Avoid eager evaluation like `logger.info("message")` as it constructs the string even when logging is disabled.

# Integration Testing
- Integration tests verify complete user workflows across the entire application stack (REST API → Use Cases → Domain Logic → Persistence).
- A dedicated `integration` Gradle module contains all integration tests, separate from any specific adapter module.
- Integration tests use `@SpringBootTest` with a dedicated `IntegrationTestConfiguration` class that explicitly scans only the required packages (api, domain, persistence).
- The `integration` module depends on: api, dungeons-domain, persistence, and app-port modules (CLI is excluded to avoid bean conflicts from its REST client implementations).
- The module also includes test classes from dungeons-domain to access SOME_X test data instances.
- MongoDB is provided via Testcontainers, ensuring tests run against a real MongoDB instance.
- The `AbstractIntegrationTest` base class provides:
  - Automatic database cleanup between tests via MongoTemplate
  - Helper methods for authentication and HTTP calls
  - RestTestClient (Spring Framework 7) for making REST API calls with fluent assertions
  - Access to MongoTemplate for direct database operations when needed
  - Configures dev profile to satisfy ProfileValidator requirements
- Integration tests use the modern RestTestClient API (Spring Boot 4.0+) instead of deprecated TestRestTemplate:
  - Fluent API: `.post().uri().body().exchange().expectStatus().expectBody()`
  - Better integration with Spring's testing infrastructure
  - Cleaner assertions and response handling
- Integration tests focus on:
  - Cross-module interactions (end-to-end workflows)
  - Security integration (JWT authentication/authorization)
  - Data consistency across layers
- Integration tests do NOT duplicate unit test coverage - they verify integration points, not individual component behavior.
- Test naming convention: Tests are suffixed with `IntegrationTest` (e.g., `GameFlowIntegrationTest`).