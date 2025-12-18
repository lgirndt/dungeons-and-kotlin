plugins {
    id("buildlogic.kotlin-library-conventions")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}

dependencies {
    // Depend on modules needed for API integration testing
    testImplementation(project(":api"))
    // Note: :cli module is excluded to avoid bean conflicts (it has REST client implementations)
    testImplementation(project(":dungeons-domain"))
    testImplementation(project(":persistence"))
    testImplementation(project(":app-port"))

    // Access test data from domain module
    testImplementation(project(path = ":dungeons-domain", configuration = "testClasses"))

    // Spring Boot testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc")
    testImplementation("org.springframework.boot:spring-boot-resttestclient") // Required for RestTestClient in Spring Boot 4.0
    testImplementation("org.springframework.security:spring-security-test")

    // MongoDB test support
    testImplementation("org.springframework.boot:spring-boot-starter-data-mongodb-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-mongodb")

    // Logging
    testImplementation("io.github.oshai:kotlin-logging-jvm")

    // Kotlin
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib")
    testImplementation("org.jetbrains.kotlin:kotlin-reflect")
}
