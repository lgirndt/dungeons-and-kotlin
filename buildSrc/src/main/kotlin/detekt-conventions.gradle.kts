plugins {
    id("dev.detekt")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom(files("$rootDir/detekt.yml"))
    baseline = file("$rootDir/detekt-baseline.xml")
    parallel = true
}

tasks.withType<dev.detekt.gradle.Detekt>().configureEach {
    reports {
        html.required.set(true)
        sarif.required.set(false)
    }
}

dependencies {
    detektPlugins("dev.detekt:detekt-rules-ktlint-wrapper:${Versions.detekt}")
}
