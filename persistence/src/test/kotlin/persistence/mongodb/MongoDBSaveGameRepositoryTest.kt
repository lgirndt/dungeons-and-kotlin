package io.dungeons.persistence.mongodb

import io.dungeons.domain.savegame.SOME_SAVE_GAME
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest
import kotlin.jvm.optionals.getOrNull
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DataMongoTest
@CleanMongoRepositories
class MongoDBSaveGameRepositoryTest {
    @Autowired
    private lateinit var repository: MongoDBSaveGameRepository

    @Test
    fun `should save and retrieve a save game`() {
        // Given
        val saveGame = SOME_SAVE_GAME.copy()

        // When
        repository.save(saveGame)

        // Then
        val found = repository.findById(saveGame.id).orElseThrow()
        assertTrue(found != null)
        assertEquals(saveGame.id, found.id)
        assertEquals(saveGame.playerId, found.playerId)
    }

    @Test
    fun `should find save game by userId and saveGameId`() {
        // Given
        val saveGame = SOME_SAVE_GAME.copy()
        repository.save(saveGame)

        // When
        val found = repository.findByUserId(saveGame.playerId, saveGame.id).orElseThrow()

        // Then
        assertTrue(found != null)
        assertEquals(saveGame.id, found.id)
        assertEquals(saveGame.playerId, found.playerId)
    }

    @Test
    fun `should not find save game with wrong userId`() {
        // Given
        val saveGame = SOME_SAVE_GAME.copy()
        repository.save(saveGame)

        val differentPlayerId = io.dungeons.domain.core.Id.generate<io.dungeons.domain.core.Player>()

        // When
        val found = repository.findByUserId(differentPlayerId, saveGame.id).getOrNull()

        // Then
        assertEquals(found, null)
    }

    @Test
    fun `should find all save games for a user`() {
        // Given
        val userId = io.dungeons.domain.core.Id.generate<io.dungeons.domain.core.Player>()
        val saveGame1 = SOME_SAVE_GAME.copy(
            id = io.dungeons.domain.core.Id.generate(),
            playerId = userId,
        )
        val saveGame2 = SOME_SAVE_GAME.copy(
            id = io.dungeons.domain.core.Id.generate(),
            playerId = userId,
        )
        val saveGame3 = SOME_SAVE_GAME.copy(
            id = io.dungeons.domain.core.Id.generate(),
            playerId = userId,
        )

        repository.save(saveGame1)
        repository.save(saveGame2)
        repository.save(saveGame3)

        // When
        val found = repository.findAllByUserId(userId)

        // Then
        assertEquals(3, found.size)
        assertTrue(found.any { it.id == saveGame1.id })
        assertTrue(found.any { it.id == saveGame2.id })
        assertTrue(found.any { it.id == saveGame3.id })
    }

    @Test
    fun `should return only save games for specified user`() {
        // Given
        val userId1 = io.dungeons.domain.core.Id.generate<io.dungeons.domain.core.Player>()
        val userId2 = io.dungeons.domain.core.Id.generate<io.dungeons.domain.core.Player>()

        val user1SaveGame1 = SOME_SAVE_GAME.copy(
            id = io.dungeons.domain.core.Id.generate(),
            playerId = userId1,
        )
        val user1SaveGame2 = SOME_SAVE_GAME.copy(
            id = io.dungeons.domain.core.Id.generate(),
            playerId = userId1,
        )
        val user2SaveGame = SOME_SAVE_GAME.copy(
            id = io.dungeons.domain.core.Id.generate(),
            playerId = userId2,
        )

        repository.save(user1SaveGame1)
        repository.save(user1SaveGame2)
        repository.save(user2SaveGame)

        // When
        val foundForUser1 = repository.findAllByUserId(userId1)

        // Then
        assertEquals(2, foundForUser1.size)
        assertTrue(foundForUser1.all { it.playerId == userId1 })
        assertTrue(foundForUser1.any { it.id == user1SaveGame1.id })
        assertTrue(foundForUser1.any { it.id == user1SaveGame2.id })
    }

    @Test
    fun `should return empty list when user has no save games`() {
        // Given
        val userId = io.dungeons.domain.core.Id.generate<io.dungeons.domain.core.Player>()

        // When
        val found = repository.findAllByUserId(userId)

        // Then
        assertTrue(found.isEmpty())
    }
}
