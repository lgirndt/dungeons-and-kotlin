plugins {
    id("buildlogic.kotlin-application-conventions")
//    id("org.springframework.boot") // version "4.0.0"
}

dependencies {
    // Version managed by common conventions
    implementation(project(":dungeons-domain"))
    implementation(project(":persistence"))
    implementation(project(":app-port"))

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    // JSON processing - Jackson 3.x for Spring Boot 4.0+
    implementation("tools.jackson.module:jackson-module-kotlin")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // JWT dependencies
    implementation("io.jsonwebtoken:jjwt-api")
    implementation("ch.qos.logback:logback-classic")
    implementation("io.github.oshai:kotlin-logging-jvm")

    runtimeOnly("io.jsonwebtoken:jjwt-impl")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")
}

application {
    // Define the main class for the application.
    mainClass = "io.dungeons.api.ApiKt"
}

tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    systemProperty("spring.profiles.active", System.getProperty("spring.profiles.active", "dev"))
}
