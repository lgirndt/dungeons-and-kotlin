package io.dungeons.persistence.mongodb

import io.dungeons.domain.core.Player
import io.dungeons.domain.savegame.SaveGame
import io.dungeons.domain.savegame.SaveGameRepository
import io.dungeons.port.Id
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MongoDBSaveGameRepository :
    SaveGameRepository,
    MongoRepository<SaveGame, Id<SaveGame>> {
    @Query("{ '_id': ?0 }")
    override fun findById(id: Id<SaveGame>): Optional<SaveGame>

    @Query(
        """
        {
            "playerId": ?0,
            "_id": ?1
        }
    """,
    )
    override fun findByUserId(userId: Id<Player>, saveGameId: Id<SaveGame>): Optional<SaveGame>

    @Query("{ 'playerId': ?0 }")
    override fun findAllByUserId(userId: Id<Player>): List<SaveGame>
}
