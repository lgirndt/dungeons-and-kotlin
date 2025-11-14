plugins {
    id("kotlin-conventions")
    id("test-deps-conventions")
}

dependencies {
    implementation("com.google.guava:guava:${Versions.guava}")

    testImplementation(kotlin("test"))
}
