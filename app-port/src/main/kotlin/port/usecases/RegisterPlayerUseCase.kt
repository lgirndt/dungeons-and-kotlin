package io.dungeons.port.usecases

import io.dungeons.port.PlayerId

data class PlayerRequest(val name: String, val password: String)

interface RegisterPlayerUseCase {
    fun execute(request: PlayerRequest): Result<PlayerId>
}
