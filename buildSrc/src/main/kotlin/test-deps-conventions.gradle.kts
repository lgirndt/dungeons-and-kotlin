plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:${Versions.junitBom}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.natpryce:hamkrest:${Versions.hamkrest}")
    testImplementation("io.mockk:mockk:${Versions.mockk}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}