plugins {
    id("buildlogic.kotlin-application-conventions")
}

dependencies {
    implementation(project(":dungeons-domain"))
    implementation(project(":persistence"))
    implementation(project(":app-port"))

    // Spring Boot (without web)
    implementation("org.springframework.boot:spring-boot-starter")

    // HTTP Client for making REST calls
    implementation("org.springframework.boot:spring-boot-starter-web-services")

    // JSON processing
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Clikt for CLI
    implementation("com.github.ajalt.clikt:clikt:5.0.3")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Development
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
}

application {
    // Define the main class for the application.
    mainClass = "io.dungeons.tool.ToolApplicationKt"
}