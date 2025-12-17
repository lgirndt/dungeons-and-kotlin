package io.dungeons.cli.screen

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
import io.dungeons.cli.GameStateHolder
import io.dungeons.port.AdventureSummaryResponse
import io.dungeons.port.ListAdventuresQuery
import io.dungeons.port.usecases.CreateNewGameRequest
import io.dungeons.port.usecases.CreateNewGameUseCase
import org.springframework.stereotype.Component

@Component
class PickAdventureScreen(
    private val listAdventuresQuery: ListAdventuresQuery,
    private val createNewGameUseCase: CreateNewGameUseCase,
    private val gameStateHolder: GameStateHolder,
) : Screen<ScreenTransition>(
    ownTransition = ScreenTransition.PickAdventure,
    defaultTransition = ScreenTransition.Exit,
) {
    private var adventures: LiveList<AdventureSummaryResponse> by InitOnce()
    private var selectedAdventure: LiveVar<Int> by InitOnce()

    override val sectionBlock: MainRenderScope.() -> Unit
        get() = {
            textLine("Pick your adventure:")
            textLine()
            adventures.forEachIndexed { index, adventure ->
                val prefix = if (index == selectedAdventure.value) "> " else "  "
                textLine("$prefix${adventure.name}")
            }
        }

    override val runBlock: RunScope.() -> Unit = {
        val scope = this
        onKeyPressed {
            when (key) {
                Keys.UP -> {
                    if (selectedAdventure.value > 0) {
                        selectedAdventure.value -= 1
                    }
                }

                Keys.DOWN -> {
                    if (selectedAdventure.value < adventures.size - 1) {
                        selectedAdventure.value += 1
                    }
                }

                Keys.ENTER -> {
                    val adventure = adventures[selectedAdventure.value]
                    createNewGame(adventure)
                    exit(scope, ScreenTransition.Room)
                }
            }
        }
    }

    override fun init(session: Session) {
        adventures = session.liveListOf(listAdventuresQuery.query())
        selectedAdventure = session.liveVarOf(0)
    }

    private fun createNewGame(adventure: AdventureSummaryResponse) {
        val playerId = gameStateHolder.gameState.playerId ?: error("Cannot create game: player not initialized")

        val gameId = createNewGameUseCase.execute(
            CreateNewGameRequest(
                playerId,
                adventure.id,
            ),
        )
        val newGameState = gameStateHolder.gameState.copy(
            currentGameId = gameId.id,
        )
        gameStateHolder.gameState = newGameState
    }
}
