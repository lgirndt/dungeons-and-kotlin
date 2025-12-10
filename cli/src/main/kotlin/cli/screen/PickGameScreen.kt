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
import io.dungeons.domain.savegame.ListSaveGamesQuery
import io.dungeons.domain.savegame.NewGameUseCase
import io.dungeons.domain.savegame.SaveGame
import org.springframework.stereotype.Component

@Component
class PickGameScreen(
    private val listSaveGamesQuery: ListSaveGamesQuery,
    private val newGameUseCase: NewGameUseCase,
    private val adventureRepository: AdventureRepository,
    private val gameStateHolder: GameStateHolder,
) : Screen<ScreenTransition>(
    ownTransition = ScreenTransition.PickGame,
    defaultTransition = ScreenTransition.Exit,
) {
    private sealed class MenuItem {
        data object NewGame : MenuItem()
        data class ExistingSave(val saveGame: SaveGame) : MenuItem()
    }

    private var menuItems: LiveList<MenuItem> by InitOnce()
    private var selectedIndex: LiveVar<Int> by InitOnce()

    override val sectionBlock: MainRenderScope.() -> Unit
        get() = {
            textLine("Select a game:")
            textLine()
            menuItems.forEachIndexed { index, item ->
                val prefix = if (index == selectedIndex.value) "> " else "  "
                val label = when (item) {
                    is MenuItem.NewGame -> "New Game"
                    is MenuItem.ExistingSave -> "Continue - Save #${item.saveGame.id}"
                }
                textLine("$prefix$label")
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

        val gameId = newGameUseCase.execute(player.id, firstAdventure)
        gameStateHolder.gameState = gameStateHolder.gameState.copy(currentGameId = gameId)
    }

    private fun loadExistingGame(saveGameId: Id<SaveGame>) {
        gameStateHolder.gameState = gameStateHolder.gameState.copy(currentGameId = saveGameId)
    }
}
