package io.dungeons.domain.player

import io.dungeons.domain.UseCaseException
import io.dungeons.port.Id
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component

data class PlayerRequest(val name: String, val password: String)

class PlayerAlreadyExistsException(message: String) : UseCaseException(message)

@Component
class RegisterPlayerUseCase(private val playerRepository: PlayerRepository) {
    fun execute(request: PlayerRequest): Result<Id<Player>> {
        try {
            val player = Player(
                id = Id.generate(),
                name = request.name,
                hashedPassword = request.password,
            )
            val insertedPlayer = playerRepository.insert(player)
            return Result.success(insertedPlayer.id)
        } catch (_: DuplicateKeyException) {
            return Result.failure(
                PlayerAlreadyExistsException("Player with name '${request.name}' already exists."),
            )
        }
    }
}
