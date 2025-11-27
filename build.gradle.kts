allprojects {
    group = "io.dungeons"
    version = "1.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "kotlin-conventions")
    apply(plugin = "test-deps-conventions")
    apply(plugin = "detekt-conventions")
}

tasks.register("test") {
    dependsOn(subprojects.map { it.tasks.named("test") })
    group = "verification"
    description = "Run tests for all subprojects"
}
