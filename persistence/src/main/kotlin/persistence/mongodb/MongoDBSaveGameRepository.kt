package io.dungeons.persistence.mongodb

import io.dungeons.domain.savegame.SaveGame
import io.dungeons.domain.savegame.SaveGameRepository
import io.dungeons.port.PlayerId
import io.dungeons.port.SaveGameId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MongoDBSaveGameRepository :
    SaveGameRepository,
    MongoRepository<SaveGame, SaveGameId> {
    @Query("{ '_id': ?0 }")
    override fun findById(id: SaveGameId): Optional<SaveGame>

    @Query(
        """
        {
            "playerId": ?0,
            "_id": ?1
        }
    """,
    )
    override fun findByUserId(userId: PlayerId, saveGameId: SaveGameId): Optional<SaveGame>

    @Query("{ 'playerId': ?0 }")
    override fun findAllByUserId(userId: PlayerId): List<SaveGame>
}
