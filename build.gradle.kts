group = "io.dungeons"
version = "1.0-SNAPSHOT"

tasks.register("test") {
    dependsOn(subprojects.map { it.tasks.named("test") })
    group = "verification"
    description = "Run tests for all subprojects"
}
