package io.dungeons.app

import app.config.ProfileValidator
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.core.env.Environment
import kotlin.time.Clock

@SpringBootApplication(scanBasePackages = ["io.dungeons"])
class App {
    private val log = LoggerFactory.getLogger(App::class.java)

    @Bean
    fun init() = CommandLineRunner {
        log.info("This is coming from the CommandLineRunner.")
    }

    @Bean
    fun clock() = Clock.System

    @Bean
    fun profileValidator(environment: Environment): ProfileValidator = ProfileValidator(environment)
}

fun main(args: Array<String>) {
    runApplication<App>()
}
