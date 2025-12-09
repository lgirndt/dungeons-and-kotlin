package io.dungeons.persistence.mongodb

import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.domain.core.Id
import io.dungeons.persistence.mongodb.entities.AdventureDocument
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class MongoDBAdventureRepository(
    private val mongoTemplate: MongoTemplate
) : AdventureRepository {
    override fun findAll(): List<Adventure> {
        return mongoTemplate
            .findAll(AdventureDocument::class.java)
            .map { it.toDomain() }
    }

    override fun findById(id: Id<Adventure>): Optional<Adventure> {
        TODO("Not yet implemented")
    }

}