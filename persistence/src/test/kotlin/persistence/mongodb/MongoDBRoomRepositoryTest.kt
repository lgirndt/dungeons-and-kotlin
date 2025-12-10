package io.dungeons.persistence.mongodb

import io.dungeons.domain.core.Id
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest
import org.springframework.context.annotation.Import
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@DataMongoTest
@CleanMongoRepositories
@Import(MongoDBRoomRepository::class)
class MongoDBRoomRepositoryTest {

    @Autowired
    private lateinit var roomRepository: MongoDBRoomRepository

    @Autowired
    private lateinit var adventureRepository: MongoDBAdventureRepository

    @Test
    fun `should find room when it exists in adventure`() {
        // Given - Create an adventure with rooms
        val room1 = SOME_ROOM.copy(id = Id.generate(), name = "Room 1")
        val room2 = SOME_ROOM.copy(id = Id.generate(), name = "Room 2")
        val adventure = SOME_ADVENTURE.copy(
            id = Id.generate(),
            rooms = listOf(room1, room2)
        )
        adventureRepository.save(adventure)

        // When
        val foundRoom = roomRepository.find(adventure.id, room1.id)

        // Then
        assertNotNull(foundRoom)
        assertEquals(room1.id, foundRoom.id)
        assertEquals(room1.name, foundRoom.name)
    }

    @Test
    fun `should return null when room does not exist in adventure`() {
        // Given - Create an adventure with one room
        val existingRoom = SOME_ROOM.copy(id = Id.generate())
        val adventure = SOME_ADVENTURE.copy(
            id = Id.generate(),
            rooms = listOf(existingRoom)
        )
        adventureRepository.save(adventure)

        // When - Try to find a room that doesn't exist
        val nonExistentRoomId = Id.generate<io.dungeons.domain.world.Room>()
        val foundRoom = roomRepository.find(adventure.id, nonExistentRoomId)

        // Then
        assertNull(foundRoom)
    }

    @Test
    fun `should return null when adventure does not exist`() {
        // Given - No adventure created
        val nonExistentAdventureId = Id.generate<io.dungeons.domain.adventure.Adventure>()
        val nonExistentRoomId = Id.generate<io.dungeons.domain.world.Room>()

        // When
        val foundRoom = roomRepository.find(nonExistentAdventureId, nonExistentRoomId)

        // Then
        assertNull(foundRoom)
    }

    @Test
    fun `should not find room from different adventure`() {
        // Given - Create two adventures, each with their own rooms
        val room1 = SOME_ROOM.copy(id = Id.generate(), name = "Adventure 1 Room")
        val adventure1 = SOME_ADVENTURE.copy(
            id = Id.generate(),
            rooms = listOf(room1)
        )
        adventureRepository.save(adventure1)

        val room2 = SOME_ROOM.copy(id = Id.generate(), name = "Adventure 2 Room")
        val adventure2 = SOME_ADVENTURE.copy(
            id = Id.generate(),
            rooms = listOf(room2)
        )
        adventureRepository.save(adventure2)

        // When - Try to find room2 in adventure1
        val foundRoom = roomRepository.find(adventure1.id, room2.id)

        // Then
        assertNull(foundRoom)
    }
}