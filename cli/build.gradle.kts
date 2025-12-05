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


    implementation("com.varabyte.kotter:kotter-jvm:1.2.1")
//    implementation("com.varabyte.kotterx:kotter-grid-jvm:1.2.1")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.varabyte.kotterx:kotter-test-support-jvm:1.2.1")
}


application {
    // Define the main class for the application.
    mainClass = "cli.CliApplicationKt"
}
