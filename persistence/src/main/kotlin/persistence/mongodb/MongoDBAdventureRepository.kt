package io.dungeons.persistence.mongodb

import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.port.AdventureId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MongoDBAdventureRepository :
    AdventureRepository,
    MongoRepository<Adventure, AdventureId> {
    @Query("{ '_id': ?0 }")
    override fun findById(id: AdventureId): Optional<Adventure>
}
