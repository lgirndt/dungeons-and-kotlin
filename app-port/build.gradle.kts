plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    // JSON processing - Jackson annotations for polymorphic types
    implementation("com.fasterxml.jackson.core:jackson-annotations")
}
