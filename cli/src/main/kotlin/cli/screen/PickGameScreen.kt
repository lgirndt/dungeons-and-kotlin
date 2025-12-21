package io.dungeons.cli.screen

import cli.ui.MenuComponent
import cli.ui.MenuConfig
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.RunScope
import com.varabyte.kotter.runtime.Session
import io.dungeons.cli.GameStateHolder
import io.dungeons.port.ListAdventuresQuery
import io.dungeons.port.ListSaveGamesQuery
import io.dungeons.port.SaveGameId
import io.dungeons.port.SaveGameSummaryResponse
import io.dungeons.port.usecases.CreateNewGameRequest
import io.dungeons.port.usecases.CreateNewGameUseCase
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import org.springframework.stereotype.Component

@Component
class PickGameScreen(
    private val listSaveGamesQuery: ListSaveGamesQuery,
    private val createNewGameUseCase: CreateNewGameUseCase,
    private val listAdventuresQuery: ListAdventuresQuery,
    private val gameStateHolder: GameStateHolder,
) : Screen<ScreenTransition>(
    ownTransition = ScreenTransition.PickGame,
    defaultTransition = ScreenTransition.Exit,
) {
    private sealed class GameChoice {
        data object NewGame : GameChoice()

        data class ExistingSave(val saveGame: SaveGameSummaryResponse) : GameChoice()
    }

    private var menu: MenuComponent<GameChoice> by InitOnce()
    private var currentRunScope: RunScope by InitOnce()

    override val sectionBlock: MainRenderScope.() -> Unit
        get() = {
            textLine("Select a game:")
            textLine()
            menu.render(this)
        }

    override val runBlock: RunScope.() -> Unit = {
        currentRunScope = this
        menu.handleInput(this)
    }

    override fun init(session: Session) {
        val playerId = gameStateHolder.gameState.playerId
            ?: error("Cannot initialize PickGameScreen: player not initialized")

        val existingSaves = listSaveGamesQuery.query(playerId)

        menu = MenuComponent.create(
            session = session,
            config = MenuConfig(
                showBreadcrumb = false,
                backKey = null, // Disable back navigation for top-level menu
            ),
            onActivate = { choice ->
                when (choice) {
                    is GameChoice.NewGame -> {
                        createNewGame()
                    }
                    is GameChoice.ExistingSave -> {
                        loadExistingGame(choice.saveGame.id)
                    }
                }
                exit(currentRunScope, ScreenTransition.Room)
            },
        ) {
            branch("Game Selection") {
                val items = mutableListOf<cli.ui.MenuItem<GameChoice>>()

                items.add(leaf("New Game", GameChoice.NewGame))

                existingSaves.forEach { save ->
                    val label = formatSaveGameLabel(save)
                    items.add(leaf(label, GameChoice.ExistingSave(save)))
                }

                items
            }
        }
    }

    private fun formatSaveGameLabel(save: SaveGameSummaryResponse): String {
        val localDateTime = save.savedAt.toLocalDateTime(TimeZone.currentSystemDefault())
        val dateTime = localDateTime.format(
            LocalDateTime.Format {
                date(
                    LocalDate.Format {
                        year()
                        char('-')
                        monthNumber()
                        char('-')
                        day()
                    },
                )
                chars(" ")
                time(
                    LocalTime.Format {
                        hour()
                        char(':')
                        minute()
                    },
                )
            },
        )
        return "Continue - Save ($dateTime)"
    }

    private fun createNewGame() {
        val playerId = gameStateHolder.gameState.playerId
            ?: error("Cannot create game: player not initialized")

        val firstAdventure = listAdventuresQuery.query().firstOrNull()
            ?: error("Cannot create game: no adventures available")

        val gameId = createNewGameUseCase.execute(
            CreateNewGameRequest(
                playerId,
                firstAdventure.id,
            ),
        )
        gameStateHolder.gameState = gameStateHolder.gameState.copy(currentGameId = gameId.id)
    }

    private fun loadExistingGame(saveGameId: SaveGameId) {
        gameStateHolder.gameState = gameStateHolder.gameState.copy(
            currentGameId = saveGameId,
        )
    }
}
