# Unit Tests
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