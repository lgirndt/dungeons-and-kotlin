package io.dungeons.tool.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.domain.core.Id
import org.springframework.stereotype.Component

@Component
class CreateAdventureCommand(
    private val adventureRepository: AdventureRepository,
) : CliktCommand(name = "create-adventure") {

    override fun help(context: Context) = "Say hello"

    override fun run() {
        val adventure = Adventure(
            id = Id.fromString("8b4dc8c3-c3d5-4484-8d4e-0b7fe85bafd4"),
            name = "New Adventure",
            initialRoomId = Id.fromString("c5b563b7-d4c6-40da-b4ac-d9d081ac0f34"),
        )
        val result = adventureRepository.save(adventure)
        echo("Created adventure: $result")
    }
}