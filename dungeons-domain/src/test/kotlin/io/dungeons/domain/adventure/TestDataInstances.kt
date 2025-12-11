package io.dungeons.domain.adventure

import io.dungeons.domain.world.SOME_ROOM
import io.dungeons.port.Id

val SOME_ADVENTURE = Adventure(
    id = Id.generate(),
    name = "Test Adventure",
    initialRoomId = SOME_ROOM.id,
    rooms = listOf(SOME_ROOM),
)
