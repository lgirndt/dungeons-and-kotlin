package io.dungeons.api.rest

import io.dungeons.api.security.PlayerDetails
import io.dungeons.port.AdventureId
import io.dungeons.port.Id
import io.dungeons.port.ListSaveGamesQuery
import io.dungeons.port.PlayerId
import io.dungeons.port.SaveGameId
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
import kotlin.time.Instant

class
GameControllerTest {
    private val createNewGameUseCase = mockk<CreateNewGameUseCase>()
    private val listSaveGamesQuery = mockk<ListSaveGamesQuery>()

    private val gameController = GameController(
        createNewGameUseCase,
        listSaveGamesQuery,
    )

    private val playerId: PlayerId = Id.generate()
    private val adventureId: AdventureId = Id.generate()
    private val saveGameId: SaveGameId = Id.generate()

    private val playerDetails = PlayerDetails(
        playerId = playerId,
        username = "testplayer",
        password = "password",
        authorities = listOf(SimpleGrantedAuthority("ROLE_USER")),
    )

    @Test
    fun `createGame returns GameCreatedResponse for valid request`() {
        val request = CreateNewGameRequest(playerId, adventureId)
        val expectedResponse = GameCreatedResponse(saveGameId)

        every { createNewGameUseCase.execute(request) } returns expectedResponse

        val response = gameController.createGame(request)

        assertEquals(expectedResponse, response)

        verify(exactly = 1) { createNewGameUseCase.execute(request) }
    }

    @Test
    fun `listAll returns list of save games for authenticated player`() {
        val now = Instant.fromEpochMilliseconds(1704151445000)
        val saveGame1 = SaveGameSummaryResponse(
            id = Id.generate(),
            savedAt = now,
        )
        val saveGame2 = SaveGameSummaryResponse(
            id = Id.generate(),
            savedAt = now,
        )
        val expectedGames = listOf(saveGame1, saveGame2)

        every { listSaveGamesQuery.query(playerId) } returns expectedGames

        val result = gameController.listAll(playerDetails)

        assertEquals(expectedGames, result)

        verify { listSaveGamesQuery.query(playerId) }
    }

    @Test
    fun `listAll returns empty list when player has no save games`() {
        every { listSaveGamesQuery.query(playerId) } returns emptyList()

        val result = gameController.listAll(playerDetails)

        assertEquals(emptyList<SaveGameSummaryResponse>(), result)

        verify { listSaveGamesQuery.query(playerId) }
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
                id = Id.generate(),
                savedAt = Instant.fromEpochMilliseconds(1704151445000),
            ),
        )

        every { listSaveGamesQuery.query(playerId) } returns expectedGames

        gameController.listAll(playerDetails)

        verify(exactly = 1) { listSaveGamesQuery.query(playerId) }
    }
}
