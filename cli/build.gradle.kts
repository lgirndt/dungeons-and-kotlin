plugins {
    id("buildlogic.kotlin-application-conventions")
    id("org.springframework.boot") version "4.0.0"
}

dependencies {
    implementation(project(":dungeons-domain"))

    // Spring Boot (without web)
    implementation("org.springframework.boot:spring-boot-starter")

    // HTTP Client for making REST calls
    implementation("org.springframework.boot:spring-boot-starter-web-services")

    // JSON processing
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

application {
    // Define the main class for the application.
    mainClass = "cli.CliApplicationKt"
}
