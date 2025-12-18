package io.dungeons.api.rest

import io.dungeons.api.security.PlayerDetails
import io.dungeons.port.*
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

    private val testPlayerId = Id.fromUUID<io.dungeons.port._Player>(UUID.randomUUID())
    private val testGameId = Id.fromUUID<io.dungeons.port._SaveGame>(UUID.randomUUID())

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
        val expectedResponse = NarratedRoomResponse(
            roomId = roomId,
            readOut = "You are in a dark dungeon",
            party = party,
        )

        every {
            narrateRoomQuery.query(testPlayerId, testGameId)
        } returns Result.success(expectedResponse)

        val response = narratorController.narrateRoom(testGameId.asStringRepresentation(), testPlayerDetails)

        assertEquals(expectedResponse, response)
        assertEquals("You are in a dark dungeon", response.readOut)
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
    fun `narrateRoom uses player id from authenticated player`() {
        val specificPlayerId = Id.fromUUID<io.dungeons.port._Player>(UUID.randomUUID())
        val playerDetails = PlayerDetails(
            playerId = specificPlayerId,
            username = "specificplayer",
            password = "password",
            authorities = listOf(SimpleGrantedAuthority("ROLE_USER")),
        )
        val party = PartyResponse(heroes = emptyList())
        val expectedResponse = NarratedRoomResponse(
            roomId = UUID.randomUUID(),
            readOut = "Test room",
            party = party,
        )

        every {
            narrateRoomQuery.query(specificPlayerId, testGameId)
        } returns Result.success(expectedResponse)

        narratorController.narrateRoom(testGameId.asStringRepresentation(), playerDetails)

        verify { narrateRoomQuery.query(specificPlayerId, testGameId) }
    }

    @Test
    fun `narrateRoom parses game id from path variable correctly`() {
        val gameIdString = testGameId.asStringRepresentation()
        val party = PartyResponse(heroes = emptyList())
        val expectedResponse = NarratedRoomResponse(
            roomId = UUID.randomUUID(),
            readOut = "Test room",
            party = party,
        )

        every {
            narrateRoomQuery.query(testPlayerId, testGameId)
        } returns Result.success(expectedResponse)

        narratorController.narrateRoom(gameIdString, testPlayerDetails)

        verify { narrateRoomQuery.query(testPlayerId, testGameId) }
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

    @Test
    fun `narrateRoom returns party with no heroes when party is empty`() {
        val party = PartyResponse(heroes = emptyList())
        val expectedResponse = NarratedRoomResponse(
            roomId = UUID.randomUUID(),
            readOut = "A dead end",
            party = party,
        )

        every {
            narrateRoomQuery.query(testPlayerId, testGameId)
        } returns Result.success(expectedResponse)

        val response = narratorController.narrateRoom(testGameId.asStringRepresentation(), testPlayerDetails)

        assertEquals(emptyList<HeroResponse>(), response.party.heroes)

        verify { narrateRoomQuery.query(testPlayerId, testGameId) }
    }
}
