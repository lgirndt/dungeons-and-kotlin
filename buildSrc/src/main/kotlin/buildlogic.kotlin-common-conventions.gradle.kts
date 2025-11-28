/*
 * Common conventions for Kotlin projects.
 * This plugin is applied to all library and application projects.
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm")
    id("dev.detekt")
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // Test dependencies
    testImplementation(kotlin("test"))
    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:1.14.6")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Detekt
    detektPlugins("dev.detekt:detekt-rules-ktlint-wrapper:2.0.0-alpha.1")
}

tasks.test {
    useJUnitPlatform()
}

// Preload MockK agent manually to avoid dynamic agent loading warnings.
//
// Starting with Java 21, dynamic agent loading is discouraged and may be disabled by default
// in future releases to improve JVM integrity (see JEP 451).
//
// MockK uses ByteBuddy for instrumentation, which typically loads agents dynamically.
// To avoid the warning:
//   "WARNING: A Java agent has been loaded dynamically"
// we preload the ByteBuddy agent using the -javaagent JVM argument.
//
// This approach avoids relying on -XX:+EnableDynamicAgentLoading, which is not recommended.
//
// References:
// - JEP 451: https://openjdk.org/jeps/451
// - Related article: https://rieckpil.de/how-to-configure-mockito-agent-for-java-21-without-warning/
// - https://github.com/mockk/mockk/issues/1171
tasks.withType<Test>().configureEach {
    doFirst {
        val mockkAgent = classpath.find {
            it.name.contains("byte-buddy-agent")
        }
        if (mockkAgent != null) {
            println("Preload MockK agent (byte-buddy-agent)")
            jvmArgs("-javaagent:${mockkAgent.absolutePath}")
        }
    }
}

// Detekt configuration
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/detekt.yml"))
    baseline = file("$rootDir/detekt-baseline.xml")
    parallel = true
}

tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
    reports {
        html.required = true
        sarif.required = false
    }
    mustRunAfter(tasks.withType<Test>())
}
