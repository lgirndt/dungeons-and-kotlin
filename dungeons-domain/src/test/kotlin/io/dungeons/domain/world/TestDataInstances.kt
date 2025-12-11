package io.dungeons.domain.world

import io.dungeons.port.Id

val SOME_ROOM = Room(
    id = Id.generate(),
    name = "Test Room",
    description = "A test room for unit tests",
    connections = emptyMap(),
)
