package io.dungeons.persistence.mongodb

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DataMongoTest
@CleanMongoRepositories
class MongoDBAdventureRepositoryTest {

    @Autowired
    private lateinit var repository: MongoDBAdventureRepository

    @Test
    fun `should save and retrieve an adventure`() {
        // Given
        val adventure = SOME_ADVENTURE.copy(id = io.dungeons.domain.core.Id.generate())

        // When
        repository.save(adventure)

        // Then
        val found = repository.findById(adventure.id).orElseThrow()
        assertTrue(found != null)
        assertEquals(adventure.id, found.id)
        assertEquals(adventure.name, found.name)
        assertEquals(adventure.initialRoomId, found.initialRoomId)
    }

    @Test
    fun `should return all saved adventures`() {
        // Given
        val adventure1 = SOME_ADVENTURE.copy(id = io.dungeons.domain.core.Id.generate())
        val adventure2 = SOME_ADVENTURE.copy(id = io.dungeons.domain.core.Id.generate())
        repository.save(adventure1)
        repository.save(adventure2)

        // When
        val allAdventures = repository.findAll()

        // Then
        assertEquals(2, allAdventures.size)
        assertTrue(allAdventures.any { it.id == adventure1.id })
        assertTrue(allAdventures.any { it.id == adventure2.id })
    }

    @Test
    fun `should delete an adventure`() {
        // Given
        val adventure = SOME_ADVENTURE.copy(id = io.dungeons.domain.core.Id.generate())
        repository.save(adventure)

        // When
        repository.delete(adventure)

        // Then
        val found = repository.findById(adventure.id)
        assertFalse(found.isPresent)
    }
}
