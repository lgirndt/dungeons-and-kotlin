plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

val kotlinVersion = "2.2.20"

dependencies {
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:$kotlinVersion")
    implementation("org.jetbrains.kotlin.plugin.spring:org.jetbrains.kotlin.plugin.spring.gradle.plugin:$kotlinVersion")
    implementation("dev.detekt:detekt-gradle-plugin:${libs.versions.detekt.get()}")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.7")
}
