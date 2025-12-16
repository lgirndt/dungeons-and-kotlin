/*
 * Common conventions for Kotlin projects.
 * This plugin is applied to all library and application projects.
 */

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    kotlin("jvm")
    kotlin("plugin.spring")

    id("dev.detekt")
    id("io.spring.dependency-management")
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
    }
}

// Centralized dependency version management
dependencyManagement {
    // Version numbers
    val junitBomVersion = "6.0.1"
    val mockkVersion = "1.14.6"
    val detektKtlintVersion = "2.0.0-alpha.1"
    val kotlinxDatetimeVersion = "0.7.1"
    val kotterVersion = "1.2.1"
    val cliktVersion = "5.0.3"
    val jjwtVersion = "0.12.6"

    imports {
        // Import JUnit BOM for test dependencies
        mavenBom("org.junit:junit-bom:$junitBomVersion")
    }

    dependencies {
        // Define versions for all project dependencies
        dependency("io.mockk:mockk:$mockkVersion")
        dependency("dev.detekt:detekt-rules-ktlint-wrapper:$detektKtlintVersion")
        dependency("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetimeVersion")

        // CLI dependencies
        dependency("com.varabyte.kotter:kotter-jvm:$kotterVersion")
        dependency("com.varabyte.kotterx:kotter-test-support-jvm:$kotterVersion")
        dependency("com.github.ajalt.clikt:clikt:$cliktVersion")

        // JWT dependencies
        dependency("io.jsonwebtoken:jjwt-api:$jjwtVersion")
        dependency("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
        dependency("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

        dependency("ch.qos.logback:logback-classic:1.5.22")
        dependency("io.github.oshai:kotlin-logging-jvm:5.1.0")
    }
}

dependencies {
    // Test dependencies - versions managed by dependencyManagement
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Detekt - version managed by dependencyManagement
    detektPlugins("dev.detekt:detekt-rules-ktlint-wrapper")
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
