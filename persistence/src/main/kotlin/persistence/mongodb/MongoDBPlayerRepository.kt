package io.dungeons.persistence.mongodb

import io.dungeons.domain.player.Player
import io.dungeons.domain.player.PlayerRepository
import io.dungeons.port.Id
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MongoDBPlayerRepository :
    PlayerRepository,
    MongoRepository<Player, Id<Player>> {
    @Query("{ '_id': ?0 }")
    override fun findById(id: Id<Player>): Optional<Player>

    @Query("{ 'name': ?0 }")
    override fun findByName(name: String): Optional<Player>
}
