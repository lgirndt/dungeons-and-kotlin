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
import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.domain.core.Id
import io.dungeons.domain.savegame.CreateNewGameUseCase
import io.dungeons.domain.savegame.ListSaveGamesQuery
import io.dungeons.domain.savegame.SaveGame
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
    private val adventureRepository: AdventureRepository,
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

        data class ExistingSave(val saveGame: SaveGame) : MenuItem() {
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
                        is MenuItem.NewGame -> createNewGame()
                        is MenuItem.ExistingSave -> loadExistingGame(selectedItem.saveGame.id)
                    }
                    exit(scope, ScreenTransition.Room)
                }
            }
        }
    }

    override fun init(session: Session) {
        val player = gameStateHolder.gameState.player
            ?: error("Cannot initialize PickGameScreen: player not initialized")

        val existingSaves = listSaveGamesQuery.execute(player.id)
        val items = mutableListOf<MenuItem>()
        items.add(MenuItem.NewGame)
        items.addAll(existingSaves.map { MenuItem.ExistingSave(it) })

        menuItems = session.liveListOf(items)
        selectedIndex = session.liveVarOf(0)
    }

    private fun createNewGame() {
        val player = gameStateHolder.gameState.player
            ?: error("Cannot create game: player not initialized")

        val firstAdventure = adventureRepository.findAll().firstOrNull()
            ?: error("Cannot create game: no adventures available")

        val gameId = createNewGameUseCase.execute(player.id, firstAdventure)
        gameStateHolder.gameState = gameStateHolder.gameState.copy(currentGameId = gameId)
    }

    private fun loadExistingGame(saveGameId: Id<SaveGame>) {
        gameStateHolder.gameState = gameStateHolder.gameState.copy(currentGameId = saveGameId)
    }
}
