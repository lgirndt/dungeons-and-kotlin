package io.dungeons.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import org.springframework.stereotype.Component

@Component
class MainCommand(private val gameLoop: GameLoop) : CliktCommand() {
    private val gameStateFile by option("--game-state-file")
        .path(mustExist = true, canBeDir = false)

    override fun run() {
        gameLoop.run(gameStateFile)
    }
}