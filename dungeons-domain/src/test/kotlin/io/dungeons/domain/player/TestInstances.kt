package io.dungeons.domain.player

import io.dungeons.port.Id

val SOME_PLAYER = Player(
    id = Id.generate(),
    name = "Some Player",
    hashedPassword = "hashed_password",
)
