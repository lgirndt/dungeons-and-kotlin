package io.dungeons.domain.savegame

import io.dungeons.domain.core.Id
import io.dungeons.domain.core.Player
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ListSaveGamesQueryTest {
    private val repository = mockk<SaveGameRepository>()
    private val query = ListSaveGamesQuery(repository)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @Test
    fun `should delegate to repository findAllByUserId`() {
        val playerId = Id.generate<Player>()
        val expectedSaves = listOf(
            SOME_SAVE_GAME.copy(),
            SOME_SAVE_GAME.copy(),
        )
        every { repository.findAllByUserId(playerId) } returns expectedSaves

        val result = query.execute(playerId)

        assertEquals(expectedSaves, result)
        verify(exactly = 1) { repository.findAllByUserId(playerId) }
    }

    @Test
    fun `should return empty list when player has no saves`() {
        val playerId = Id.generate<Player>()
        every { repository.findAllByUserId(playerId) } returns emptyList()

        val result = query.execute(playerId)

        assertEquals(emptyList(), result)
    }
}
