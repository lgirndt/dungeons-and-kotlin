package io.dungeons.api.rest

import io.dungeons.api.security.PlayerDetails
import io.dungeons.port.HeroResponse
import io.dungeons.port.Id
import io.dungeons.port.NarrateRoomQuery
import io.dungeons.port.NarratedRoomResponse
import io.dungeons.port.PartyResponse
import io.dungeons.port.PlayerId
import io.dungeons.port.SaveGameId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

class NarratorControllerTest {
    private val narrateRoomQuery = mockk<NarrateRoomQuery>()

    private val narratorController = NarratorController(narrateRoomQuery)

    private val testPlayerId : PlayerId = Id.generate()
    private val testGameId : SaveGameId = Id.generate()

    private val testPlayerDetails = PlayerDetails(
        playerId = testPlayerId,
        username = "testplayer",
        password = "password",
        authorities = listOf(SimpleGrantedAuthority("ROLE_USER")),
    )

    @Test
    fun `narrateRoom returns NarratedRoomResponse for valid request`() {
        val roomId = UUID.randomUUID()
        val party = PartyResponse(heroes = listOf(HeroResponse("Gandalf")))
        val readOut = "You are in a dark dungeon"
        val expectedResponse = NarratedRoomResponse(
            roomId = roomId,
            readOut = readOut,
            party = party,
        )

        every {
            narrateRoomQuery.query(testPlayerId, testGameId)
        } returns Result.success(expectedResponse)

        val response = narratorController.narrateRoom(testGameId.asStringRepresentation(), testPlayerDetails)

        assertEquals(expectedResponse, response)
        assertEquals(readOut, response.readOut)
        assertEquals(roomId, response.roomId)

        verify { narrateRoomQuery.query(testPlayerId, testGameId) }
    }

    @Test
    fun `narrateRoom delegates to query with correct parameters`() {
        val party = PartyResponse(heroes = emptyList())
        val expectedResponse = NarratedRoomResponse(
            roomId = UUID.randomUUID(),
            readOut = "A bright room",
            party = party,
        )

        every {
            narrateRoomQuery.query(testPlayerId, testGameId)
        } returns Result.success(expectedResponse)

        narratorController.narrateRoom(testGameId.asStringRepresentation(), testPlayerDetails)

        verify(exactly = 1) { narrateRoomQuery.query(testPlayerId, testGameId) }
    }

    @Test
    fun `narrateRoom throws exception when query fails`() {
        val expectedException = IllegalStateException("Game not found")

        every {
            narrateRoomQuery.query(testPlayerId, testGameId)
        } returns Result.failure(expectedException)

        val exception = assertThrows(IllegalStateException::class.java) {
            narratorController.narrateRoom(testGameId.asStringRepresentation(), testPlayerDetails)
        }

        assertEquals("Game not found", exception.message)

        verify { narrateRoomQuery.query(testPlayerId, testGameId) }
    }

}
