plugins {
    id("buildlogic.kotlin-library-conventions")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":dungeons-domain"))

    // MongoDB
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
//    implementation("org.springframework.boot:spring-boot-starter-mongodb")


    // Kotlin
//    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Testing
//    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
