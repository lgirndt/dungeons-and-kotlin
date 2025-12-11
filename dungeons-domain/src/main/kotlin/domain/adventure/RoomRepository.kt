package io.dungeons.domain.adventure

import io.dungeons.domain.world.Room
import io.dungeons.port.Id

interface RoomRepository {
    fun find(adventureId: Id<Adventure>, roomId: Id<Room>): Room?
}
