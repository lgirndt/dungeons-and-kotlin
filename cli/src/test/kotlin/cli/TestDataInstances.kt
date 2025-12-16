package io.dungeons.cli

import io.dungeons.port.Id

val SOME_GAME_STATE = GameState(
    playerId = Id.generate(),
    currentGameId = Id.generate(),
    authToken = "test-auth-token-12345"
)