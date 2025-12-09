package io.dungeons.tool

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication(scanBasePackages = ["io.dungeons"])
class ToolApplication {
    private val logger = LoggerFactory.getLogger(ToolApplication::class.java)

    @Bean
    fun runner() = CommandLineRunner { args ->
        logger.info("Starting tool application with arguments: ${args.joinToString()}")
        ToolCli()
            .subcommands(HelloCommand())
            .main(args)
    }
}

class ToolCli : CliktCommand(name = "tool") {
    override fun help(context: Context) = "D&D Tool CLI Application"

    override fun run() {
        // No-op: this is just the parent command
    }
}

class HelloCommand : CliktCommand(name = "hello") {
    private val name by option("--name", "-n").help("Name to greet")

    override fun help(context: Context) = "Say hello"

    override fun run() {
        val greeting = if (name != null) {
            "Hello, $name!"
        } else {
            "Hello, World!"
        }
        echo(greeting)
    }
}

fun main(args: Array<String>) {
    runApplication<ToolApplication>(*args)
}
