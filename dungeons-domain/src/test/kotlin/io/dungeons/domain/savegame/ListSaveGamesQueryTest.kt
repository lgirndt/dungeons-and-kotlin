package io.dungeons.domain.savegame

import io.dungeons.port.Id
import io.dungeons.port.PlayerId
import io.dungeons.port.SaveGameSummaryResponse
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ListSaveGamesQueryTest {
    private val repository = mockk<SaveGameRepository>()
    private val query = ListSaveGamesQueryImpl(repository)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `should delegate to repository findAllByUserId`() {
        val playerId: PlayerId = Id.generate()
        val saveGames = listOf(
            SOME_SAVE_GAME.copy(),
            SOME_SAVE_GAME.copy(),
        )
        every { repository.findAllByUserId(playerId) } returns saveGames

        val result = query.query(playerId)

        val expected = saveGames.map {
            SaveGameSummaryResponse(
                id = it.id.castTo(),
                savedAt = it.savedAt,
            )
        }
        assertEquals(expected, result)
        verify(exactly = 1) { repository.findAllByUserId(playerId) }
    }

    @Test
    fun `should return empty list when player has no saves`() {
        val playerId: PlayerId = Id.generate()
        every { repository.findAllByUserId(playerId) } returns emptyList()

        val result = query.query(playerId)

        assertEquals(emptyList(), result)
    }
}
