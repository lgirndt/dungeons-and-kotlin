package app.config

import jakarta.annotation.PostConstruct
import org.springframework.core.env.Environment

class ProfileValidator(private val environment: Environment) {

    @PostConstruct
    fun validateProfiles() {
        val activeProfiles = environment.activeProfiles.toSet()
        val requiredProfiles = setOf("dev", "prod")
        
        val matchingProfiles = activeProfiles.intersect(requiredProfiles)
        
        when {
            matchingProfiles.isEmpty() -> {
                throw IllegalStateException(
                    "No environment profile active. Please activate exactly one of: ${requiredProfiles.joinToString(", ")}."
                )
            }
            matchingProfiles.size > 1 -> {
                throw IllegalStateException(
                    "Multiple environment profiles active: ${matchingProfiles.joinToString(", ")}. " +
                    "Please activate only one of: ${requiredProfiles.joinToString(", ")}"
                )
            }
            else -> {
                println("Application started with profile: ${matchingProfiles.first()}")
            }
        }
    }
}
