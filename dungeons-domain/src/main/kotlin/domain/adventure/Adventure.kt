package io.dungeons.domain.adventure

import io.dungeons.domain.world.Room
import io.dungeons.port.AdventureId
import io.dungeons.port.RoomId

data class Adventure(val id: AdventureId, val name: String, val initialRoomId: RoomId, val rooms: List<Room>)
