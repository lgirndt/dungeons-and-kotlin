package io.dungeons.tool.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.dungeons.domain.core.Id
import org.springframework.stereotype.Component

@Component
class GenIdCommand : CliktCommand(name = "gen-id") {
    private val count by option("--count", "-c")
        .int()
        .default(1)
        .help("Number of IDs to generate")

    override fun help(context: Context) = "Generate a new unique ID"

    override fun run() {
        repeat(count) {
            val newId = Id.Companion.generate<String>()
            echo(newId.asStringRepresentation())
        }
    }
}