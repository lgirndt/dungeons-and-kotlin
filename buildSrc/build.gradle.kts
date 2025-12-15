plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

val kotlinVersion = "2.2.20"
val springBootVersion = "4.0.0"
val detektVersion = "2.0.0-alpha.1"

dependencies {
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin.plugin.spring:org.jetbrains.kotlin.plugin.spring.gradle.plugin:$kotlinVersion")
    implementation("dev.detekt:detekt-gradle-plugin:$detektVersion")
    implementation("org.springframework.boot:org.springframework.boot.gradle.plugin:$springBootVersion")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.7")
}
