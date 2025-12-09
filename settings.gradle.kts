plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

rootProject.name = "learn-kotlin"

include("dungeons-domain")
include("app")
include("persistence")
include("cli")
include("tool")
