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