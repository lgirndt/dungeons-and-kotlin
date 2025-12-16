plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {

    implementation(project(":app-port"))
    implementation("org.jetbrains.kotlinx:kotlinx-datetime")
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")

    // Logging
    implementation("ch.qos.logback:logback-classic")
    implementation("io.github.oshai:kotlin-logging-jvm")
}

// Create a configuration to expose test classes to other modules
val testClasses by configurations.creating {
    extendsFrom(configurations.testImplementation.get())
}

tasks.register<Jar>("testJar") {
    archiveClassifier.set("tests")
    from(sourceSets.test.get().output)
}

artifacts {
    add("testClasses", tasks.named<Jar>("testJar"))
}
