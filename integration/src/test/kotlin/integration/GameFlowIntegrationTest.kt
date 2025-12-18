package io.dungeons.integration

import io.dungeons.api.rest.dto.AuthResponse
import io.dungeons.api.rest.dto.AuthenticationRequest
import io.dungeons.domain.player.PlayerRepository
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration test for the complete game creation workflow.
 *
 * Tests the full stack integration using RestTestClient (Spring Boot 4.0+):
 * - REST API layer (controllers)
 * - Security layer (JWT authentication)
 * - Use cases (business logic)
 * - Persistence layer (MongoDB repositories)
 */
class GameFlowIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var playerRepository: PlayerRepository

    @Test
    fun `complete game creation flow - register, login, create game, and list games`() {
        // Given: A registered and authenticated player
        val playerName = "adventurer"
        val token = registerAndAuthenticate(playerName = playerName, password = "secret123")

        // And: An adventure exists in the database
        val adventure = testData.adventure()
        val playerId = getPlayerIdByName(playerName)

        // When: Creating a new game
        val createGameRequest = CreateNewGameRequest(
            playerId = playerId,
            adventureId = adventure.id,
        )

        val createdGame = authenticatedPost(
            endpoint = "/game",
            body = createGameRequest,
            token = token,
        ).expectOkAndExtract(GameCreatedResponse::class.java)

        // Then: The game appears in the player's game list
        val games = authenticatedGet(
            endpoint = "/games",
            token = token,
        ).expectOkAndExtractList(object : ParameterizedTypeReference<List<SaveGameSummaryResponse>>() {})

        assertEquals(1, games.size, "Player should have exactly one game")

        val game = games.first()
        assertEquals(createdGame.id, game.id, "Listed game should match created game")
        assertNotNull(game.savedAt, "Game should have a saved timestamp")
    }

    @Test
    fun `cannot create game without authentication`() {
        // Given: An adventure exists
        val adventure = testData.adventure()
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
    fun `player can only see their own games`() {
        // Given: Two different players
        val player1Token = registerAndAuthenticate(playerName = "player1", password = "pass1")
        val player2Token = registerAndAuthenticate(playerName = "player2", password = "pass2")

        val player1Id = getPlayerIdByName("player1")
        val player2Id = getPlayerIdByName("player2")

        val adventure = testData.adventure()

        // When: Each player creates a game
        authenticatedPost(
            endpoint = "/game",
            body = CreateNewGameRequest(playerId = player1Id, adventureId = adventure.id),
            token = player1Token,
        ).expectStatus().isOk

        authenticatedPost(
            endpoint = "/game",
            body = CreateNewGameRequest(playerId = player2Id, adventureId = adventure.id),
            token = player2Token,
        ).expectStatus().isOk

        // Then: Each player sees only their own game
        val player1Games = authenticatedGet(
            endpoint = "/games",
            token = player1Token,
        ).expectOkAndExtractList(object : ParameterizedTypeReference<List<SaveGameSummaryResponse>>() {})

        val player2Games = authenticatedGet(
            endpoint = "/games",
            token = player2Token,
        ).expectOkAndExtractList(object : ParameterizedTypeReference<List<SaveGameSummaryResponse>>() {})

        assertEquals(1, player1Games.size, "Player 1 should see only their game")
        assertEquals(1, player2Games.size, "Player 2 should see only their game")
        assertTrue(
            player1Games.first().id != player2Games.first().id,
            "Players should have different games",
        )
    }

    @Test
    fun `player can login and get room narration for their saved game`() {
        // Given: A player, adventure, and save game exist
        val playerName = "hero"
        val playerPassword = "heropassword"
        val player = testData.player(name = playerName, password = playerPassword)
        val adventure = testData.adventure(name = "The Dark Cave")
        val saveGame = testData.saveGame(
            playerId = player.id,
            adventureId = adventure.id,
        )

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
}
