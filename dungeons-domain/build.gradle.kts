plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    implementation(libs.guava)
    implementation("org.springframework:spring-context")
    implementation("org.springframework.data:spring-data-commons")
}
