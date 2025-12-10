package io.dungeons.tool.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.domain.adventure.RoomRepository
import org.springframework.stereotype.Component

@Component
class HelloCommand(private val adventureRepository: AdventureRepository, private val roomRepository: RoomRepository) :
    CliktCommand(name = "hello") {
    private val name by option("--name", "-n").help("Name to greet")

    override fun help(context: Context) = "Say hello"

    override fun run() {
        val greeting = if (name != null) {
            "Hello, $name!"
        } else {
            "Hello, World!"
        }
        echo(greeting)

        val adventures = adventureRepository.findAll()
        echo("You have ${adventures.size} adventures in the repository.")

        if (adventures.isEmpty()) {
            echo("No adventures found, skipping room lookup.")
            return
        }

        val adventure = adventures.first()
        echo("The adventure is called '${adventure.name}'.")

        val room = roomRepository.find(
            adventureId = adventure.id,
            roomId = adventure.initialRoomId,
        )
        echo("I found the room '${room?.name ?: "<not found>"}' in the adventure.")
    }
}
