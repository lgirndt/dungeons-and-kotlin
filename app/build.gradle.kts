plugins {
    id("buildlogic.kotlin-application-conventions")
    id("org.springframework.boot") version "4.0.0"
}

dependencies {
    // Version managed by common conventions
    implementation("com.google.guava:guava")
    implementation(project(":dungeons-domain"))

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    
    // JWT dependencies
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

application {
    // Define the main class for the application.
    mainClass = "io.dungeons.app.AppKt"
}
