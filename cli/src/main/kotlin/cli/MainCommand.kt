package io.dungeons.cli

import com.github.ajalt.clikt.core.CliktCommand
import org.springframework.stereotype.Component

@Component
class MainCommand(private val gameLoop: GameLoop) : CliktCommand() {
    override fun run() {
        gameLoop.run(null)
    }
}