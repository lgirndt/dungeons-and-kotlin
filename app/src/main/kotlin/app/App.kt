package io.dungeons.app

import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class App {

    private val log = LoggerFactory.getLogger(App::class.java)

    @Bean
    fun init() = CommandLineRunner {
        log.info("This is coming from the CommandLineRunner.")
    }
}

fun main(args: Array<String>) {
    runApplication<App>()
}
