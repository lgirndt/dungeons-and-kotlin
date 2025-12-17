package io.dungeons.tool

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import io.dungeons.tool.commands.CreateTestDataCommand
import io.dungeons.tool.commands.GenIdCommand
import io.dungeons.tool.commands.HelloCommand
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.time.Clock

private val logger = KotlinLogging.logger {}

@SpringBootApplication(scanBasePackages = ["io.dungeons"])
class ToolApplication {
    @Bean
    fun clock() = Clock.System

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        val idForEncode = "bcrypt"
        return DelegatingPasswordEncoder(
            idForEncode,
            mapOf(
                idForEncode to BCryptPasswordEncoder(12),
            ),
        )
    }

    @Bean
    fun runner(genIdCommand: GenIdCommand, helloCommand: HelloCommand, createTestDataCommand: CreateTestDataCommand) =
        CommandLineRunner { args ->
            logger.info { "Starting tool application with arguments: ${args.joinToString()}" }
            ToolCli()
                .subcommands(
                    genIdCommand,
                    helloCommand,
                    createTestDataCommand,
                )
                .main(args)
        }
}

class ToolCli : CliktCommand(name = "tool") {
    override fun help(context: Context) = "D&D Tool CLI Application"

    override fun run() {
        // No-op: this is just the parent command
    }
}

fun main(args: Array<String>) {
    runApplication<ToolApplication>(*args)
}
