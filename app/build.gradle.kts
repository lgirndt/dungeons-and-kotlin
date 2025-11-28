plugins {
    id("buildlogic.kotlin-application-conventions")
}

dependencies {
    implementation(libs.guava)
    implementation(project(":dungeons-domain"))
}

application {
    // Define the main class for the application.
    mainClass = "io.dungeons.app.MainKt"
}
