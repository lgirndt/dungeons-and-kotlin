package io.dungeons.persistence.mongodb

import io.dungeons.domain.player.Player
import io.dungeons.domain.player.SOME_PLAYER
import io.dungeons.port.Id
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest
import org.springframework.dao.DuplicateKeyException
import kotlin.jvm.optionals.getOrNull
import kotlin.test.assertEquals

@DataMongoTest
@CleanMongoRepositories
class MongoDBPlayerRepositoryTest {

    @Autowired
    private lateinit var playerRepository: MongoDBPlayerRepository

    @Test
    fun `insert should insert player into database if it does not yet exist`() {
        val id = Id.generate<Player>()
        val player = SOME_PLAYER.copy(id = id)
        playerRepository.insert(player)

        val loadedPlayer = playerRepository.findById(id)
        assertEquals(player, loadedPlayer.getOrNull())
    }

    @Test
    fun `insert should fail to insert player into database if it already exists`() {
        val id = Id.generate<Player>()
        val player = SOME_PLAYER.copy(id = id)
        playerRepository.insert(player)

        assertThrows<DuplicateKeyException> {
            playerRepository.insert(player)
        }
    }

    @Test
    fun `findByName should return player when found`() {
        val id = Id.generate<Player>()
        val name = "UniqueName"
        val player = SOME_PLAYER.copy(id = id, name = name)
        playerRepository.insert(player)

        val loadedPlayer = playerRepository.findByName(name)
        assertEquals(player, loadedPlayer.getOrNull())
    }
}