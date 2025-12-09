package io.dungeons.persistence.mongodb

import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.adventure.RoomRepository
import io.dungeons.domain.core.Id
import io.dungeons.domain.world.Room
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.Aggregation.match
import org.springframework.data.mongodb.core.aggregation.Aggregation.project
import org.springframework.data.mongodb.core.aggregation.Aggregation.replaceRoot
import org.springframework.data.mongodb.core.aggregation.Aggregation.unwind
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Repository

@Repository
class MongoDBRoomRepository(private val mongoTemplate: MongoTemplate) : RoomRepository {
    override fun find(adventureId: Id<Adventure>, roomId: Id<Room>): Room? {
        val aggregation = Aggregation.newAggregation(
            match(Criteria.where(ID_FIELD).isEqualTo(adventureId.value)),
            project(ROOMS_FIELD),
            unwind(ROOMS_FIELD),
            match(Criteria.where("$ROOMS_FIELD.$ID_FIELD").isEqualTo(roomId.value)),
            replaceRoot(ROOMS_FIELD),
        )

        return mongoTemplate.aggregate<Room>(aggregation, ADVENTURE_COLLECTION).uniqueMappedResult
    }

    companion object {
        private const val ID_FIELD = "_id"
        private const val ROOMS_FIELD = "rooms"
        private const val ADVENTURE_COLLECTION = "adventure"
    }
}
