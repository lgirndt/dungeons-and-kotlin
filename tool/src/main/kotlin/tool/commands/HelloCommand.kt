package io.dungeons.tool.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option

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