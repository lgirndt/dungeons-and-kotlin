package io.dungeons.domain.savegame

import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.core.Id
import io.dungeons.domain.core.Player

interface CreateNewGameUseCase {
    fun execute(userId: Id<Player>, adventure: Adventure): Id<SaveGame>
}
