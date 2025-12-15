package io.dungeons.domain.adventure

import io.dungeons.domain.world.Room
import io.dungeons.port.AdventureId
import io.dungeons.port.RoomId

interface RoomRepository {
    fun find(adventureId: AdventureId, roomId: RoomId): Room?
}
