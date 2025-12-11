package io.dungeons.domain.adventure

import io.dungeons.domain.world.Room
import io.dungeons.port.Id

data class Adventure(val id: Id<Adventure>, val name: String, val initialRoomId: Id<Room>, val rooms: List<Room>)
