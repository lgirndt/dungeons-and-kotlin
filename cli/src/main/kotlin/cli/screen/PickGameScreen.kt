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
import io.dungeons.domain.savegame.CreateNewGameUseCase
import io.dungeons.port.Id
import io.dungeons.port.ListAdventuresQuery
import io.dungeons.port.ListSaveGamesQuery
import io.dungeons.port.SaveGameSummaryResponse
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
    private sealed class MenuItem {
        abstract val label: String

        data object NewGame : MenuItem() {
            override val label: String
                get() = "New Game"
        }

        data class ExistingSave(val saveGame: SaveGameSummaryResponse) : MenuItem() {
            override val label: String
                get() {
                    val localDateTime = saveGame.savedAt.toLocalDateTime(TimeZone.currentSystemDefault())
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
        }
    }

    private var menuItems: LiveList<MenuItem> by InitOnce()
    private var selectedIndex: LiveVar<Int> by InitOnce()

    override val sectionBlock: MainRenderScope.() -> Unit
        get() = {
            textLine("Select a game:")
            textLine()
            menuItems.forEachIndexed { index, item ->
                val prefix = if (index == selectedIndex.value) "> " else "  "
                textLine("$prefix${item.label}")
            }
        }

    override val runBlock: RunScope.() -> Unit = {
        val scope = this
        onKeyPressed {
            when (key) {
                Keys.UP -> {
                    if (selectedIndex.value > 0) {
                        selectedIndex.value -= 1
                    }
                }

                Keys.DOWN -> {
                    if (selectedIndex.value < menuItems.size - 1) {
                        selectedIndex.value += 1
                    }
                }

                Keys.ENTER -> {
                    when (val selectedItem = menuItems[selectedIndex.value]) {
                        is MenuItem.NewGame -> {
                            createNewGame()
                        }
                        is MenuItem.ExistingSave -> {
                            val saveGameId = Id.fromUUID<io.dungeons.domain.savegame.SaveGame>(
                                selectedItem.saveGame.id,
                            )
                            loadExistingGame(saveGameId)
                        }
                    }
                    exit(scope, ScreenTransition.Room)
                }
            }
        }
    }

    override fun init(session: Session) {
        val player = gameStateHolder.gameState.player
            ?: error("Cannot initialize PickGameScreen: player not initialized")

        val existingSaves = listSaveGamesQuery.query(player.id.toUUID())
        val items = mutableListOf<MenuItem>()
        items.add(MenuItem.NewGame)
        items.addAll(existingSaves.map { MenuItem.ExistingSave(it) })

        menuItems = session.liveListOf(items)
        selectedIndex = session.liveVarOf(0)
    }

    private fun createNewGame() {
        val player = gameStateHolder.gameState.player
            ?: error("Cannot create game: player not initialized")

        val firstAdventure = listAdventuresQuery.query().firstOrNull()
            ?: error("Cannot create game: no adventures available")

        val gameId = createNewGameUseCase.execute(
            player.id.toUUID(),
            firstAdventure.id,
            firstAdventure.initialRoomId,
        )
        gameStateHolder.gameState = gameStateHolder.gameState.copy(currentGameId = gameId)
    }

    private fun loadExistingGame(saveGameId: Id<*>) {
        @Suppress("UNCHECKED_CAST")
        gameStateHolder.gameState = gameStateHolder.gameState.copy(
            currentGameId = saveGameId as Id<io.dungeons.domain.savegame.SaveGame>,
        )
    }
}
