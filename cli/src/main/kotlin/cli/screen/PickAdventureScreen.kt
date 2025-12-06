package cli.screen

import cli.GameStateHolder
import com.varabyte.kotter.foundation.LiveVar
import com.varabyte.kotter.foundation.collections.LiveList
import com.varabyte.kotter.foundation.collections.liveListOf
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.input.onKeyPressed
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.RunScope
import com.varabyte.kotter.runtime.Session
import io.dungeons.domain.adventure.Adventure
import io.dungeons.domain.adventure.ListAdventuresQuery
import io.dungeons.domain.savegame.NewGameUseCase
import org.springframework.stereotype.Component
import java.util.*

@Component
class PickAdventureScreen(
    private val listAdventuresQuery: ListAdventuresQuery,
    private val newGameUseCase: NewGameUseCase,
    private val gameStateHolder: GameStateHolder
) : Screen<ScreenTransition>(
    ownTransition = ScreenTransition.PickAdventure,
    defaultTransition = ScreenTransition.Exit,
) {

    private lateinit var adventures: LiveList<Adventure>
    private lateinit var selectedAvdenture: LiveVar<Int>

    override fun init(session: Session) {
        adventures = session.liveListOf(listAdventuresQuery.execute())
        selectedAvdenture = session.liveVarOf(0)
    }

    override val sectionBlock: MainRenderScope.() -> Unit
        get() = {
            textLine("Pick your adventure:")
            textLine()
            adventures.forEachIndexed { index, adventure ->
                val prefix = if (index == selectedAvdenture.value) "> " else "  "
                textLine("$prefix${adventure.name}")
            }
        }

    override val runBlock: RunScope.() -> Unit = {
        val scope = this
        onKeyPressed {
            when (key) {
                Keys.UP -> {
                    if (selectedAvdenture.value > 0) {
                        selectedAvdenture.value -= 1
                    }
                }

                Keys.DOWN -> {
                    if (selectedAvdenture.value < adventures.size - 1) {
                        selectedAvdenture.value += 1
                    }
                }

                Keys.ENTER -> {
                    val adventure = adventures[selectedAvdenture.value]
                    createNewGame(adventure)
                    exit(scope, ScreenTransition.MyScreen)
                }
            }
        }
    }

    private fun createNewGame(adventure: Adventure) {
        val player = gameStateHolder.gameState.player
        require(player != null)

        val userId : UUID = player.id.toUUID()
        val gameId = newGameUseCase.execute(userId, adventure)
        val newGameState = gameStateHolder.gameState.copy(
            currentGameId = gameId,
        )
        gameStateHolder.gameState = newGameState
    }

}