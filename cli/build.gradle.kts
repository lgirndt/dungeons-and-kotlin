plugins {
    id("buildlogic.kotlin-application-conventions")
//    id("org.springframework.boot") // version "4.0.0"
}

dependencies {
//    implementation(project(":dungeons-domain"))
//    implementation(project(":persistence"))
    implementation(project(":app-port"))

    // Spring Boot (without web)
    implementation("org.springframework.boot:spring-boot-starter")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime")

    // HTTP Client for making REST calls
    implementation("org.springframework.boot:spring-boot-starter-web-services")

    // JSON processing - Jackson 3.x for Spring Boot 4.0+
    implementation("tools.jackson.module:jackson-module-kotlin")

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Clikt for CLI
    implementation("com.github.ajalt.clikt:clikt")

    implementation("com.varabyte.kotter:kotter-jvm")

    implementation("ch.qos.logback:logback-classic")
    implementation("io.github.oshai:kotlin-logging-jvm")

//    implementation("com.varabyte.kotterx:kotter-grid-jvm")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.varabyte.kotterx:kotter-test-support-jvm")

    // Development
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
}

application {
    // Define the main class for the application.
    mainClass = "io.dungeons.cli.CliKt"
}
