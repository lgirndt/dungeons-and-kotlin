/*
 * Convention plugin for Kotlin application projects.
 */

plugins {
    // Apply the common convention plugin for shared build configuration between library and application projects.
    id("buildlogic.kotlin-common-conventions")
    id("org.springframework.boot")

    // Apply the application plugin to add support for building a CLI application.
    application
}
