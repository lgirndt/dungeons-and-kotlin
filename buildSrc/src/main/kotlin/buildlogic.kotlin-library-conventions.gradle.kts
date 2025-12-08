import org.springframework.boot.gradle.tasks.bundling.BootJar

/*
 * Convention plugin for Kotlin library projects.
 */

plugins {
    // Apply the common convention plugin for shared build configuration between library and application projects.
    id("buildlogic.kotlin-common-conventions")
    id("org.springframework.boot")
    // Apply the java-library plugin for API and implementation separation.
    `java-library`
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}