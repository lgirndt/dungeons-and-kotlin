package io.dungeons.domain.savegame

import io.dungeons.port.Id
import java.util.*

interface CreateNewGameUseCase {
    fun execute(playerId: UUID, adventureId: UUID): Id<SaveGame>
}
