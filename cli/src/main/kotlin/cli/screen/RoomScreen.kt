package io.dungeons.cli.screen

import cli.ui.MenuComponent
import cli.ui.MenuConfig
import com.varabyte.kotter.foundation.LiveVar
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.RunScope
import com.varabyte.kotter.runtime.Session
import io.dungeons.cli.GameStateHolder
import io.dungeons.port.DoorId
import io.dungeons.port.LeaveRoomAction
import io.dungeons.port.NarrateRoomQuery
import io.dungeons.port.NarratedRoomResponse
import io.dungeons.port.RoomAction
import org.springframework.stereotype.Component

@Component
class RoomScreen(private val narrateRoomQuery: NarrateRoomQuery, private val gameStateHolder: GameStateHolder) :
    Screen<ScreenTransition>(
        ownTransition = ScreenTransition.Room,
        defaultTransition = ScreenTransition.Exit,
    ) {
    private var room: LiveVar<NarratedRoomResponse> by InitOnce()

    private var actionMenu: MenuComponent<RoomAction> by InitOnce()

    override val sectionBlock: MainRenderScope.() -> Unit
        get() = {
            textLine(room.value.readOut)
            textLine()
            textLine("What do you want to do?")
            textLine()
            for (hero in room.value.party.heroes) {
                text("[${hero.name}] ")
            }
            textLine()
            actionMenu.render(this)
        }

    override val runBlock: RunScope.() -> Unit = {
        actionMenu.handleInput(this)
    }

    override fun init(session: Session) {
        val (playerId, gameId) = gameStateHolder.unpackIdsOrThrow()
        val narratedRoom = narrateRoomQuery.query(
            playerId,
            gameId,
        ).getOrThrow()

        room = session.liveVarOf(narratedRoom)
        actionMenu = createActionMenu(session)
    }

    private fun createActionMenu(session: Session): MenuComponent<RoomAction> = MenuComponent.create(
        session = session,
        config = MenuConfig(),
        onActivate = { action ->
            // TODO handle action
        },
    ) {
        branch("Available Actions") {
            listOf(
                branch("Moving") {
                    listOf(
                        leaf("Leave Room", LeaveRoomAction(DoorId.generate(), PLACEHOLDER_DESCRIPTION)),
                        leaf("Navigate", LeaveRoomAction(DoorId.generate(), PLACEHOLDER_DESCRIPTION)),
                    )
                },
                branch("Other Actions") {
                    listOf(
                        leaf("Look Around", LeaveRoomAction(DoorId.generate(), PLACEHOLDER_DESCRIPTION)),
                        leaf("Check Inventory", LeaveRoomAction(DoorId.generate(), PLACEHOLDER_DESCRIPTION)),
                    )
                },
            )
        }
    }

    companion object {
        private const val PLACEHOLDER_DESCRIPTION = "foo bar"
    }
}
