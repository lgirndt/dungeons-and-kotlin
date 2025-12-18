package io.dungeons.api.rest

import io.dungeons.port.AdventureSummaryResponse
import io.dungeons.port.Id
import io.dungeons.port.ListAdventuresQuery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.*

class AdventureSummaryControllerTest {
    private val listAdventuresQuery = mockk<ListAdventuresQuery>()

    private val adventureSummaryController = AdventureSummaryController(listAdventuresQuery)

    @Test
    fun `findAll returns list of adventure summaries`() {
        val adventure1 = AdventureSummaryResponse(
            id = Id.fromUUID(UUID.randomUUID()),
            name = "Dragon's Lair",
        )
        val adventure2 = AdventureSummaryResponse(
            id = Id.fromUUID(UUID.randomUUID()),
            name = "Goblin Camp",
        )
        val expectedAdventures = listOf(adventure1, adventure2)

        every { listAdventuresQuery.query() } returns expectedAdventures

        val result = adventureSummaryController.findAll()

        assertEquals(expectedAdventures, result)
        assertEquals(2, result.size)

        verify { listAdventuresQuery.query() }
    }

    @Test
    fun `findAll returns empty list when no adventures exist`() {
        every { listAdventuresQuery.query() } returns emptyList()

        val result = adventureSummaryController.findAll()

        assertEquals(emptyList<AdventureSummaryResponse>(), result)

        verify { listAdventuresQuery.query() }
    }

    @Test
    fun `findAll delegates to query exactly once`() {
        val expectedAdventures = listOf(
            AdventureSummaryResponse(
                id = Id.fromUUID(UUID.randomUUID()),
                name = "Test Adventure",
            ),
        )

        every { listAdventuresQuery.query() } returns expectedAdventures

        adventureSummaryController.findAll()

        verify(exactly = 1) { listAdventuresQuery.query() }
    }

    @Test
    fun `findAll returns adventures with correct structure`() {
        val adventureId = Id.fromUUID<io.dungeons.port._Adventure>(UUID.randomUUID())
        val adventure = AdventureSummaryResponse(
            id = adventureId,
            name = "The Lost Temple",
        )

        every { listAdventuresQuery.query() } returns listOf(adventure)

        val result = adventureSummaryController.findAll()

        assertEquals(1, result.size)
        assertEquals(adventureId, result[0].id)
        assertEquals("The Lost Temple", result[0].name)

        verify { listAdventuresQuery.query() }
    }
}
