package io.dungeons.domain.savegame

import io.dungeons.domain.core.Id
import io.dungeons.domain.core.Player
import org.junit.jupiter.api.Test
import kotlin.jvm.optionals.getOrNull
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MockSaveGameRepositoryTest {
    @Test
    fun `should save and retrieve a save game by id`() {
        // Given
        val repository = MockSaveGameRepository()
        val saveGame = SaveGame(
            playerId = Id.generate(),
            adventureId = Id.generate(),
            currentRoomId = Id.generate(),
        )

        // When
        repository.save(saveGame)

        // Then
        val found = repository.findByUserId(saveGame.playerId, saveGame.id).getOrNull()
        assertNotNull(found)
        assertEquals(saveGame.id, found.id)
        assertEquals(saveGame.playerId, found.playerId)
    }

    @Test
    fun `should support multiple save games per user`() {
        // Given
        val repository = MockSaveGameRepository()
        val userId = Id.generate<Player>()
        val saveGame1 = SaveGame(
            playerId = userId,
            adventureId = Id.generate(),
            currentRoomId = Id.generate(),
        )
        val saveGame2 = SaveGame(
            playerId = userId,
            adventureId = Id.generate(),
            currentRoomId = Id.generate(),
        )

        // When
        repository.save(saveGame1)
        repository.save(saveGame2)

        // Then
        val found1 = repository.findByUserId(userId, saveGame1.id).getOrNull()
        val found2 = repository.findByUserId(userId, saveGame2.id).getOrNull()
        assertNotNull(found1)
        assertNotNull(found2)
        assertEquals(saveGame1.id, found1.id)
        assertEquals(saveGame2.id, found2.id)
    }

    @Test
    fun `should return empty when save game id does not exist`() {
        // Given
        val repository = MockSaveGameRepository()
        val saveGame = SaveGame(
            playerId = Id.generate(),
            adventureId = Id.generate(),
            currentRoomId = Id.generate(),
        )
        repository.save(saveGame)

        val nonExistentSaveGameId = Id.generate<SaveGame>()

        // When
        val found = repository.findByUserId(saveGame.playerId, nonExistentSaveGameId).getOrNull()

        // Then
        assertNull(found)
    }

    @Test
    fun `should return empty when user id does not match`() {
        // Given
        val repository = MockSaveGameRepository()
        val saveGame = SaveGame(
            playerId = Id.generate(),
            adventureId = Id.generate(),
            currentRoomId = Id.generate(),
        )
        repository.save(saveGame)

        val differentUserId = Id.generate<Player>()

        // When
        val found = repository.findByUserId(differentUserId, saveGame.id).getOrNull()

        // Then
        assertNull(found)
    }

    @Test
    fun `should find all save games for a user`() {
        // Given
        val repository = MockSaveGameRepository()
        val userId = Id.generate<Player>()
        val saveGame1 = SaveGame(
            playerId = userId,
            adventureId = Id.generate(),
            currentRoomId = Id.generate(),
        )
        val saveGame2 = SaveGame(
            playerId = userId,
            adventureId = Id.generate(),
            currentRoomId = Id.generate(),
        )

        repository.save(saveGame1)
        repository.save(saveGame2)

        // When
        val found = repository.findAllByUserId(userId)

        // Then
        assertEquals(2, found.size)
        assertTrue(found.any { it.id == saveGame1.id })
        assertTrue(found.any { it.id == saveGame2.id })
    }

    @Test
    fun `should return only save games for specified user`() {
        // Given
        val repository = MockSaveGameRepository()
        val userId1 = Id.generate<Player>()
        val userId2 = Id.generate<Player>()

        val user1SaveGame = SaveGame(
            playerId = userId1,
            adventureId = Id.generate(),
            currentRoomId = Id.generate(),
        )
        val user2SaveGame = SaveGame(
            playerId = userId2,
            adventureId = Id.generate(),
            currentRoomId = Id.generate(),
        )

        repository.save(user1SaveGame)
        repository.save(user2SaveGame)

        // When
        val found = repository.findAllByUserId(userId1)

        // Then
        assertEquals(1, found.size)
        assertEquals(user1SaveGame.id, found.first().id)
    }

    @Test
    fun `should return empty list when user has no save games`() {
        // Given
        val repository = MockSaveGameRepository()
        val userId = Id.generate<Player>()

        // When
        val found = repository.findAllByUserId(userId)

        // Then
        assertTrue(found.isEmpty())
    }
}
