package io.dungeons.domain.player

import io.dungeons.domain.UseCaseException
import io.dungeons.port.Id
import io.dungeons.port.PlayerId
import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Component

class PlayerAlreadyExistsException(message: String) : UseCaseException(message)

@Component
class RegisterPlayerUseCaseImpl(private val playerRepository: PlayerRepository) : RegisterPlayerUseCase {
    override fun execute(request: PlayerRequest): Result<PlayerId> {
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
