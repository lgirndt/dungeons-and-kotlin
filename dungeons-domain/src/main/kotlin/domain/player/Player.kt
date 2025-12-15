package io.dungeons.domain.player

import io.dungeons.port.PlayerId

data class Player(val id: PlayerId, val name: String, val hashedPassword: String)
