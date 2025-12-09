package io.dungeons.domain.adventure

import io.dungeons.domain.core.Id
import io.dungeons.domain.world.Room

class Adventure(val id: Id<Adventure>, val name: String, val initialRoomId: Id<Room>)
