package io.dungeons.domain.adventure

import io.dungeons.domain.core.Id
import io.dungeons.domain.world.Room
import org.springframework.data.annotation.Id as MongoId

data class Adventure(@MongoId val id: Id<Adventure>, val name: String, val initialRoomId: Id<Room>, val rooms: List<Room>)
