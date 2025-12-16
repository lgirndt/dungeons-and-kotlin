package io.dungeons.api

import api.config.ProfileValidator
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import kotlin.time.Clock

private val logger = KotlinLogging.logger {}

@SpringBootApplication(scanBasePackages = ["io.dungeons"])
class Api {
    @Bean
    fun init() = CommandLineRunner {
        logger.info { "This is coming from the CommandLineRunner." }
    }

    @Bean
    fun clock() = Clock.System

    @Bean
    fun profileValidator(environment: Environment): ProfileValidator = ProfileValidator(environment)
}

fun main(args: Array<String>) {
    runApplication<Api>()
}
