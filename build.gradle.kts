plugins {
    kotlin("jvm") version "2.2.20"
}

group = "io.dungeons"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.google.guava:guava:33.5.0-jre")

    testImplementation(kotlin("test"))
    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.natpryce:hamkrest:1.8.0.1")
    testImplementation("io.mockk:mockk:1.14.6")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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

kotlin {
    jvmToolchain(24)
}