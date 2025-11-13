plugins {
    id("kotlin-conventions")
}

group = "io.dungeons"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("com.google.guava:guava:${Versions.guava}")

    testImplementation(kotlin("test"))
    testImplementation(platform("org.junit:junit-bom:${Versions.junitBom}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.natpryce:hamkrest:${Versions.hamkrest}")
    testImplementation("io.mockk:mockk:${Versions.mockk}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
