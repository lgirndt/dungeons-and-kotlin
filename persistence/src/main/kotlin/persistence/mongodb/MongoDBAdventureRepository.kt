package io.dungeons.persistence.mongodb

import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.domain.core.Id
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface MongoDBAdventureRepository :
    AdventureRepository,
    MongoRepository<Adventure, Id<Adventure>>
