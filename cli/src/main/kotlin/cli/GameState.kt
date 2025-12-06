package cli

import io.dungeons.domain.core.Id
import io.dungeons.domain.savegame.SaveGame
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicReference


data class Player(val id: Id<Player>)


data class GameState(
    var player: Player? = null,
    var currentGameId: Id<SaveGame>? = null
) {
}

@Component
class GameStateHolder {
    private var _gameState: AtomicReference<GameState> = AtomicReference(GameState())

    var gameState: GameState
        get() = _gameState.get()
        set(value) {
            _gameState.set(value)
        }
}