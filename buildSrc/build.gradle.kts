plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.gradle.plugin)
    implementation("dev.detekt:detekt-gradle-plugin:${libs.versions.detekt.get()}")
    implementation("io.spring.gradle:dependency-management-plugin:1.1.7")
}
