package io.dungeons.integration

import io.dungeons.api.rest.dto.AuthResponse
import io.dungeons.api.rest.dto.AuthenticationRequest
import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.domain.adventure.SOME_ADVENTURE
import io.dungeons.domain.player.Player
import io.dungeons.domain.player.PlayerRepository
import io.dungeons.domain.player.SOME_PLAYER
import io.dungeons.domain.savegame.SOME_SAVE_GAME
import io.dungeons.domain.savegame.SaveGameRepository
import io.dungeons.domain.world.Room
import io.dungeons.domain.world.WorldBuilder
import io.dungeons.port.Id
import io.dungeons.port.NarratedRoomResponse
import io.dungeons.port.PlayerId
import io.dungeons.port.SaveGameSummaryResponse
import io.dungeons.port.usecases.CreateNewGameRequest
import io.dungeons.port.usecases.GameCreatedResponse
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Integration test for the complete game creation workflow.
 *
 * Tests the full stack integration using RestTestClient (Spring Boot 4.0+):
 * - REST API layer (controllers)
 * - Security layer (JWT authentication)
 * - Use cases (business logic)
 * - Persistence layer (MongoDB repositories)
 */
class GameFlowIntegrationTest(

) : AbstractIntegrationTest() {

    @Autowired
    private lateinit var playerRepository: PlayerRepository

    @Autowired
    private lateinit var adventureRepository: AdventureRepository

    @Autowired
    private lateinit var saveGameRepository: SaveGameRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Test
    fun `complete game creation flow - register, login, create game, and list games`() {
        // Given: A registered and authenticated player
        val playerName = "adventurer"
        val token = registerAndAuthenticate(playerName = playerName, password = "secret123")

        // And: An adventure exists in the database
        val adventure = persistAdventure()
        val playerId = getPlayerIdByName(playerName)

        // When: Creating a new game
        val createGameRequest = CreateNewGameRequest(playerId, adventure.id)

        val createdGame = authenticatedPost(
            endpoint = "/game",
            body = createGameRequest,
            token = token,
        ).expectOkAndExtract(GameCreatedResponse::class.java)

        // Then: The game appears in the player's game list
        val typeReference = typeReference<SaveGameSummaryResponse>()
        val games = authenticatedGet(endpoint = "/games", token = token)
            .expectStatus()
            .isOk
            .expectBody(typeReference)
            .returnResult()
            .responseBody
            ?: error("No response body")

        assertEquals(1, games.size, "Player should have exactly one game")

        val game = games.first()
        assertEquals(createdGame.id, game.id, "Listed game should match created game")
        assertNotNull(game.savedAt, "Game should have a saved timestamp")
    }

    @Test
    fun `cannot create game without authentication`() {
        // Given: An adventure exists
        val adventure = persistAdventure()

        val playerId: PlayerId = Id.generate()

        // When: Attempting to create a game without authentication
        val createGameRequest = CreateNewGameRequest(
            playerId = playerId,
            adventureId = adventure.id,
        )

        restTestClient
            .post()
            .uri(url("/game"))
            .contentType(MediaType.APPLICATION_JSON)
            .body(createGameRequest)
            .exchange()
            // Then: Request is rejected
            .expectStatus()
            .isForbidden
    }

    @Test
    fun `player can login and get room narration for their saved game`() {
        // Given: A player, adventure, and save game exist
        val playerName = "hero"
        val playerPassword = "heropassword"
        val player = persistPlayer(playerName, playerPassword)

        val adventure = persistAdventure()
        val saveGame = SOME_SAVE_GAME.copy(
            id = Id.generate(),
            playerId = player.id,
            adventureId = adventure.id,
            currentRoomId = adventure.rooms.first().id,
        )
        saveGameRepository.save(saveGame)

        // When: Player logs in (player already exists, so we just authenticate)
        val token = authenticateExistingPlayer(playerName, playerPassword)

        // And: Gets the room narration for their current room
        val narration = authenticatedGet(
            endpoint = "/game/${saveGame.id.asStringRepresentation()}/narrator/room",
            token = token,
        ).expectOkAndExtract(NarratedRoomResponse::class.java)

        // Then: The narration contains the room information
        assertNotNull(narration.readOut, "Narration should have a readOut")
        assertEquals(adventure.initialRoomId.value, narration.roomId, "Narration should be for the current room")
        assertNotNull(narration.party, "Narration should include party information")
    }

    private fun persistPlayer(playerName: String, playerPassword: String): Player {
        val player = SOME_PLAYER.copy(
            id = Id.generate(),
            name = playerName,
            hashedPassword = passwordEncoder.encode(playerPassword) ?: error("Failed to encode password"),
        )
        playerRepository.insert(player)
        return player
    }

    /**
     * Authenticate an existing player (doesn't register, just logs in)
     */
    private fun authenticateExistingPlayer(playerName: String, password: String): String {
        val loginResponse = restTestClient
            .post()
            .uri(url("/auth/login"))
            .contentType(MediaType.APPLICATION_JSON)
            .body(AuthenticationRequest(username = playerName, password = password))
            .exchange()
            .expectOkAndExtract(AuthResponse::class.java)

        return loginResponse.accessToken
    }

    private fun getPlayerIdByName(playerName: String): PlayerId =
        playerRepository.findByName(playerName)
            .orElseThrow { IllegalStateException("Player '$playerName' not found in database") }
            .id

    private fun anAdventure(roomCount: Int = 1): Adventure {
        require(roomCount >= 1) { "Room count must be greater than 1" }
        val worldBuilder = WorldBuilder()
        0.until(roomCount).forEach { i ->
            worldBuilder.room(
                x = i,
                y = 0,
                Room(
                    id = Id.generate(),
                    name = "Room $i",
                    description = "This is room number $i.",
                ),
            )
        }
        val rooms = worldBuilder.build().rooms
        val adventure = SOME_ADVENTURE.copy(
            id = Id.generate(),
            rooms = rooms,
            initialRoomId = rooms.first().id,

            )
        return adventure
    }

    private fun persistAdventure(): Adventure {
        val adventure = anAdventure()
        adventureRepository.save(adventure)
        return adventure
    }

    private inline fun <reified T> typeReference() = object : ParameterizedTypeReference<List<T>>(){}

}
