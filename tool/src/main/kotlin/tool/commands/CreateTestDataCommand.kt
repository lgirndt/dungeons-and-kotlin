package io.dungeons.tool.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.domain.player.Player
import io.dungeons.domain.player.PlayerRepository
import io.dungeons.domain.savegame.SaveGame
import io.dungeons.domain.savegame.SaveGameRepository
import io.dungeons.domain.world.Room
import io.dungeons.domain.world.WorldBuilder
import io.dungeons.port.AdventureId
import io.dungeons.port.Id
import io.dungeons.port.PlayerId
import io.dungeons.port.RoomId
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.data.mongodb.core.index.Index
import org.springframework.data.mongodb.core.indexOps
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import kotlin.time.Clock

private val logger = KotlinLogging.logger {}

private val ADVENTURE_ID: AdventureId = Id.fromString("8b4dc8c3-c3d5-4484-8d4e-0b7fe85bafd4")

private val PLAYER_ID: PlayerId = Id.fromString("609cb790-d8b5-4a97-830f-0200fee465ab")
private val INITIAL_ROOM_ID: RoomId = Id.fromString("ae894d71-b501-42fd-b1a3-213e2e82f79c")

@Component
class CreateTestDataCommand(
    private val adventureRepository: AdventureRepository,
    private val playerRepository: PlayerRepository,
    private val passwordEncoder: PasswordEncoder,
    private val mongoOperations: MongoOperations,
    private val saveGameRepository: SaveGameRepository,
) : CliktCommand(name = "create-test-data") {
    private val clock = Clock.System

    override fun help(context: Context) = "Say hello"

    override fun run() {
        dropDatabase()
        logger.info { "Creating test data..." }
        createAdventure()
        createPlayers()
        createSaveGames()
        logger.info { "Test data creation complete." }
    }

    private fun dropDatabase() {
        logger.info { "Dropping database" }
        mongoOperations.execute { db -> db.drop() }
    }

    private fun createPlayers() {
        mongoOperations
            .indexOps<Player>()
            .createIndex(
                Index()
                    .on("name", Sort.Direction.ASC)
                    .unique(),
            )

        val players = listOf(
            Player(
                id = PLAYER_ID,
                name = "user",
                hashedPassword = "password",
            ),
        )
        players.forEach {
            val actuallyHashedPassword = passwordEncoder.encode(it.hashedPassword) ?: error("Failure")
            logger.info { "Create Player ${it.name}" }
            playerRepository.insert(
                Player(
                    id = it.id,
                    name = it.name,
                    hashedPassword = actuallyHashedPassword,
                ),
            )
        }
    }

    private fun createAdventure() {
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
            id = ADVENTURE_ID,
            name = "New Adventure",
            initialRoomId = INITIAL_ROOM_ID,
            rooms = world.rooms,
        )
        val result = adventureRepository.save(adventure)
        logger.info { "Created adventure: ${result?.name}" }
    }

    private fun createSaveGames() {
        val saveGames = listOf(
            SaveGame(
                id = Id.fromString("d1f5e8c2-3c4b-4f5a-9e6d-7c8b9a0b1c2d"),
                playerId = PLAYER_ID,
                adventureId = ADVENTURE_ID,
                currentRoomId = INITIAL_ROOM_ID,
                savedAt = clock.now(),
            ),
        )
        saveGames.forEach {
            logger.info { "Creating SaveGame for player ${it.playerId}" }
            saveGameRepository.save(it)
        }
    }
}
