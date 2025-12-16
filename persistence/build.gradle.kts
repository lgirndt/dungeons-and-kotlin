plugins {
    id("buildlogic.kotlin-library-conventions")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":dungeons-domain"))
    implementation(project(":app-port"))


    // MongoDB
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("ch.qos.logback:logback-classic")
    implementation("io.github.oshai:kotlin-logging-jvm")

    // Kotlin (required for Spring Data MongoDB Kotlin extensions)
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-data-mongodb-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    // testcontainers version should be managed by spring-boot-dependencies
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-mongodb")
    // Test dependencies - access to test data from domain module
    testImplementation(project(path = ":dungeons-domain", configuration = "testClasses"))
}
