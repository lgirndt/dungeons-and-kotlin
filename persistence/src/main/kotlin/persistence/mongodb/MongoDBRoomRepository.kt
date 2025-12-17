package io.dungeons.persistence.mongodb

import io.dungeons.domain.adventure.RoomRepository
import io.dungeons.domain.world.Room
import io.dungeons.port.AdventureId
import io.dungeons.port.RoomId
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
import java.util.*
import java.util.Optional.ofNullable

@Repository
class MongoDBRoomRepository(private val mongoTemplate: MongoTemplate) : RoomRepository {
    override fun find(adventureId: AdventureId, roomId: RoomId): Optional<Room> {

        @Suppress("StringLiteralDuplication")
        val aggregation = Aggregation.newAggregation(
            match(Criteria.where("_id").isEqualTo(adventureId.value)),
            project("rooms"),
            unwind("rooms"),
            match(Criteria.where("rooms._id").isEqualTo(roomId.value)),
            replaceRoot("rooms"),
        )

        return ofNullable(
            mongoTemplate
                .aggregate<Room>(aggregation, "adventure")
                .uniqueMappedResult,
        )
    }
}
