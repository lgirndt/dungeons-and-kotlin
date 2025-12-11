plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    implementation(libs.guava)
    implementation("org.springframework:spring-context")
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
