group = "io.dungeons"
version = "1.0-SNAPSHOT"

dependencies {
    implementation("com.google.guava:guava:${Versions.guava}")
    implementation((project(":dungeons-domain")))
}
