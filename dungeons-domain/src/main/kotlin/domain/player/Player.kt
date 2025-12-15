package io.dungeons.domain.player

import io.dungeons.port.Id

data class Player(val id: Id<Player>, val name: String, val hashedPassword: String)
