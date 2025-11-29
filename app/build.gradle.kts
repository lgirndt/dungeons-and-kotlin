plugins {
    id("buildlogic.kotlin-application-conventions")
    id("org.springframework.boot") version "4.0.0"
}

dependencies {
    // Version managed by common conventions
    implementation("com.google.guava:guava")
    implementation(project(":dungeons-domain"))

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

application {
    // Define the main class for the application.
    mainClass = "io.dungeons.app.MainKt"
}
