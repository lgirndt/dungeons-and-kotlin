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

## Running Tests

### Using the Test Runner (bin/run_tests.main.kts)

We provide a wrapper script around Gradle that provides concise, token-efficient output for test execution. This is especially useful for AI agents running tests.

**Usage:**
```bash
# Run all tests in the project
./bin/run_tests.main.kts

# Run tests for a specific module
./bin/run_tests.main.kts -m persistence

# Run a specific test
./bin/run_tests.main.kts -t "MongoDBSaveGameRepositoryTest.should save and retrieve a save game"
```

**Output Behavior:**
- **Success (exit 0)**: Prints `✓ All tests passed successfully`
- **Compilation Error (exit 1)**: Shows full Gradle output with compilation error details
- **Test Failure (exit 1)**: Shows concise failure information:
  - Test class and method name
  - Failure message (truncated if too long)
  - Relevant stack trace lines (framework noise filtered out)

**Example Success:**
```
✓ All tests passed successfully
```

**Example Test Failure:**
```
✗ Build/tests failed

Failed Tests (1):

1. io.dungeons.persistence.mongodb.MongoDBSaveGameRepositoryTest.should save and retrieve a save game()
   Message: expected: <wrong-id> but was: <Id(value=a5638bd6-0aa8-474b-8b92-8bc859be044e)>
   Stack trace:
     org.opentest4j.AssertionFailedError: expected: <wrong-id> but was: <Id(value=a5638bd6...)>
     at kotlin.test.junit5.JUnit5Asserter.assertEquals(JUnitSupport.kt:32)
     at kotlin.test.AssertionsKt__AssertionsKt.assertEquals(Assertions.kt:63)
```

**Example Compilation Error:**
```
✗ Build/tests failed

Compilation error detected. Gradle output:

> Task :persistence:compileTestKotlin FAILED
e: file:///.../MongoDBSaveGameRepositoryTest.kt:14:18 Unresolved reference 'undefinedVariable'.

FAILURE: Build failed with an exception.
...
```

### Direct Gradle Usage

You can also run tests directly with Gradle:
```bash
# Run all tests
./gradlew test

# Run tests for a specific module
./gradlew :persistence:test

# Run a specific test
./gradlew :persistence:test --tests "MongoDBSaveGameRepositoryTest"
```

Note: Direct Gradle usage produces more verbose output. The test runner script (bin/run_tests.main.kts) is preferred for concise feedback.