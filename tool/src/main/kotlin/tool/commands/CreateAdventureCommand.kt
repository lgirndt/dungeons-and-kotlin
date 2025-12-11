package io.dungeons.tool.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.domain.world.Room
import io.dungeons.domain.world.WorldBuilder
import io.dungeons.port.Id
import org.springframework.stereotype.Component

@Component
class CreateAdventureCommand(private val adventureRepository: AdventureRepository) :
    CliktCommand(name = "create-adventure") {
    override fun help(context: Context) = "Say hello"

    override fun run() {
        val world = WorldBuilder()
            .room(
                x = 1,
                y = 1,
                room = Room(
                    id = Id.fromString("ae894d71-b501-42fd-b1a3-213e2e82f79c"),
                    name = "Starting Room",
                    description = "You are in a small, dimly lit room. There is a door",
                ),
            )
            .room(
                x = 1,
                y = 2,
                room = Room(
                    id = Id.fromString("5a06b938-4e0c-4a8b-b2e4-94167417b395"),
                    name = "Hallway",
                    description = "A long hallway stretches before you. There are doors to the north and south.",
                ),
            )
            .room(
                x = 2,
                y = 1,
                room = Room(
                    id = Id.fromString("4bfb93d0-a064-4c1b-993e-8211639569d5"),
                    name = "Treasure Room",
                    description = "A room glittering with gold and jewels. You've found the treasure!",
                ),
            )
            .build()

        val adventure = Adventure(
            id = Id.fromString("8b4dc8c3-c3d5-4484-8d4e-0b7fe85bafd4"),
            name = "New Adventure",
            initialRoomId = Id.fromString("ae894d71-b501-42fd-b1a3-213e2e82f79c"),
            rooms = world.rooms,
        )
        val result = adventureRepository.save(adventure)
        echo("Created adventure: $result")
    }
}
