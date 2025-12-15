package api.config

import io.dungeons.api.rest.WorldController
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment

private val logger = LoggerFactory.getLogger(WorldController::class.java)

class ProfileValidator(private val environment: Environment) {
    @PostConstruct
    fun validateProfiles() {
        val activeProfiles = environment.activeProfiles.toSet()
        val requiredProfiles = setOf("dev", "prod")

        val matchingProfiles = activeProfiles.intersect(requiredProfiles)

        when {
            matchingProfiles.isEmpty() -> {
                throw IllegalStateException(
                    "No environment profile active. Please activate exactly one of: " +
                        "${requiredProfiles.joinToString(", ")}.",
                )
            }
            matchingProfiles.size > 1 -> {
                throw IllegalStateException(
                    "Multiple environment profiles active: ${matchingProfiles.joinToString(", ")}. " +
                        "Please activate only one of: ${requiredProfiles.joinToString(", ")}",
                )
            }
            else -> {
                logger.info("Application started with profile: ${matchingProfiles.first()}")
            }
        }
    }
}
