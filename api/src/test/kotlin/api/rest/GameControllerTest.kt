package io.dungeons.api.rest

import io.dungeons.api.security.PlayerDetails
import io.dungeons.port.Id
import io.dungeons.port.ListSaveGamesQuery
import io.dungeons.port.SaveGameSummaryResponse
import io.dungeons.port.usecases.CreateNewGameRequest
import io.dungeons.port.usecases.CreateNewGameUseCase
import io.dungeons.port.usecases.GameCreatedResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*
import kotlin.time.Instant

class GameControllerTest {
    private val createNewGameUseCase = mockk<CreateNewGameUseCase>()
    private val listSaveGamesQuery = mockk<ListSaveGamesQuery>()

    private val gameController = GameController(
        createNewGameUseCase,
        listSaveGamesQuery,
    )

    private val testPlayerId = Id.fromUUID<io.dungeons.port._Player>(UUID.randomUUID())
    private val testAdventureId = Id.fromUUID<io.dungeons.port._Adventure>(UUID.randomUUID())
    private val testSaveGameId = Id.fromUUID<io.dungeons.port._SaveGame>(UUID.randomUUID())

    private val testPlayerDetails = PlayerDetails(
        playerId = testPlayerId,
        username = "testplayer",
        password = "password",
        authorities = listOf(SimpleGrantedAuthority("ROLE_USER")),
    )

    @Test
    fun `createGame returns GameCreatedResponse for valid request`() {
        val request = CreateNewGameRequest(testPlayerId, testAdventureId)
        val expectedResponse = GameCreatedResponse(testSaveGameId)

        every { createNewGameUseCase.execute(request) } returns expectedResponse

        val response = gameController.createGame(request)

        assertEquals(expectedResponse, response)
        assertEquals(testSaveGameId, response.id)

        verify { createNewGameUseCase.execute(request) }
    }

    @Test
    fun `createGame delegates to use case with correct request`() {
        val request = CreateNewGameRequest(testPlayerId, testAdventureId)
        val expectedResponse = GameCreatedResponse(testSaveGameId)

        every { createNewGameUseCase.execute(request) } returns expectedResponse

        gameController.createGame(request)

        verify(exactly = 1) { createNewGameUseCase.execute(request) }
    }

    @Test
    fun `listAll returns list of save games for authenticated player`() {
        val now = Instant.fromEpochMilliseconds(1704151445000)
        val saveGame1 = SaveGameSummaryResponse(
            id = Id.fromUUID(UUID.randomUUID()),
            savedAt = now,
        )
        val saveGame2 = SaveGameSummaryResponse(
            id = Id.fromUUID(UUID.randomUUID()),
            savedAt = now,
        )
        val expectedGames = listOf(saveGame1, saveGame2)

        every { listSaveGamesQuery.query(testPlayerId) } returns expectedGames

        val result = gameController.listAll(testPlayerDetails)

        assertEquals(expectedGames, result)
        assertEquals(2, result.size)

        verify { listSaveGamesQuery.query(testPlayerId) }
    }

    @Test
    fun `listAll returns empty list when player has no save games`() {
        every { listSaveGamesQuery.query(testPlayerId) } returns emptyList()

        val result = gameController.listAll(testPlayerDetails)

        assertEquals(emptyList<SaveGameSummaryResponse>(), result)

        verify { listSaveGamesQuery.query(testPlayerId) }
    }

    @Test
    fun `listAll throws IllegalStateException when player is null`() {
        val exception = assertThrows(IllegalStateException::class.java) {
            gameController.listAll(null)
        }

        assertEquals("Player id is null", exception.message)
    }

    @Test
    fun `listAll queries with correct player id`() {
        val expectedGames = listOf(
            SaveGameSummaryResponse(
                id = Id.fromUUID(UUID.randomUUID()),
                savedAt = Instant.fromEpochMilliseconds(1704151445000),
            ),
        )

        every { listSaveGamesQuery.query(testPlayerId) } returns expectedGames

        gameController.listAll(testPlayerDetails)

        verify(exactly = 1) { listSaveGamesQuery.query(testPlayerId) }
    }
}
