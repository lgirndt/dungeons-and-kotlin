package io.dungeons.domain.adventure

import io.dungeons.domain.core.Id
import io.dungeons.domain.world.Room

interface RoomRepository {
    fun find(adventureId: Id<Adventure>, roomId: Id<Room>): Room?
}
