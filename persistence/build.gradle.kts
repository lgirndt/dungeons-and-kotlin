plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    implementation(project(":dungeons-domain"))

    // MongoDB
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")

    // Kotlin
//    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Testing
//    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
