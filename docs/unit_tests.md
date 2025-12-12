# Unit Tests

## General Principles
- There is a respect Test file for every Kotlin file.
- Each function and class should have corresponding unit tests.
- Each test needs to be written with maintainability in mind.
- To archive readability, we use utility objects to create the test setup
    - There needs to be a prototype object for a data class X called SOME_X
    - SOME_X will be used by calling the copy constructor to create an instance
    - of this class for a test setup
    - Only the properties that are relevant to the test are set explicitly in the
      test
    - The goal is, that the test is readable. The reader is not distracted by arbitrary
      setup information, but only with information that matters.
    - Therefore SOME_X is instantiated with some values that lead to a valid instance. But
      There values may not be relevant in the unit test and it may not be relied upon.

## Test Data Organization

### SOME_X Prototype Instances Location
- Each SOME_X test data instance is defined in the **test scope** of the module where class X is defined
- Test data instances are organized in `TestDataInstances.kt` files, located in the same package as the domain object they represent
- For example:
    - `SOME_STAT_BOCK` is in `dungeons-domain/src/test/kotlin/io/dungeons/domain/TestDataInstances.kt` (same package as `StatBlock`)
    - `SOME_PLAYER` is in `dungeons-domain/src/test/kotlin/io/dungeons/domain/core/TestDataInstances.kt` (same package as `Player`)
    - `SOME_ROOM` is in `dungeons-domain/src/test/kotlin/io/dungeons/domain/world/TestDataInstances.kt` (same package as `Room`)
    - `SOME_ADVENTURE` is in `dungeons-domain/src/test/kotlin/io/dungeons/domain/adventure/TestDataInstances.kt` (same package as `Adventure`)
    - `SOME_SAVE_GAME` is in `dungeons-domain/src/test/kotlin/io/dungeons/domain/savegame/TestDataInstances.kt` (same package as `SaveGame`)

### Using Test Data from Other Modules
- Other modules that need to use test data instances in their tests include the test package of the domain module as a dependency
- Example: The `persistence` module includes the `dungeons-domain` test classes via:
  ```kotlin
  testImplementation(project(path = ":dungeons-domain", configuration = "testClasses"))
  ```

## Running Tests

We have the skill run-unit-tests defined in `.claude/skills/run-unit-tests/SKILL.md` that provides a convenient way to 
run tests with concise output.