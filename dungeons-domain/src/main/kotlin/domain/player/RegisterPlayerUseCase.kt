package io.dungeons.domain.player

import io.dungeons.port.Id
import org.springframework.stereotype.Component

data class PlayerRequest(
    val name: String,
    val password: String
)

@Component
class RegisterPlayerUseCase(
    private val playerRepository: PlayerRepository,
) {
    fun execute(request: PlayerRequest): Id<Player> {
        try {
            val player = Player(
                id = Id.generate(),
                name = request.name,
                hashedPassword = request.password,
            )
            val insertedPlayer = playerRepository.insert(player)
            return insertedPlayer.id
        }catch(e: Exception){
            throw e
        }
    }
}