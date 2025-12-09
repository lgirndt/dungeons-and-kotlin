package io.dungeons.tool.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.domain.core.Id
import io.dungeons.domain.world.Room
import io.dungeons.domain.world.WorldBuilder
import org.springframework.stereotype.Component

@Component
class CreateAdventureCommand(private val adventureRepository: AdventureRepository) :
    CliktCommand(name = "create-adventure") {
    override fun help(context: Context) = "Say hello"

    override fun run() {
        // -- ae894d71-b501-42fd-b1a3-213e2e82f79c
        // -- 5a06b938-4e0c-4a8b-b2e4-94167417b395
        // 4bfb93d0-a064-4c1b-993e-8211639569d5
        // 38e64f06-e7b8-4129-aa83-15e64599783b
        // 7c19e0c7-2000-4ff8-8c78-7e118e3d7a2b
        // d328d97a-0371-47f7-97fc-4027769f7e1b
        // e2de34c2-b16d-41a9-bd28-2516a12a5f22
        // 6b751b53-20b9-4fa6-948a-ac5fccea9be7
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
