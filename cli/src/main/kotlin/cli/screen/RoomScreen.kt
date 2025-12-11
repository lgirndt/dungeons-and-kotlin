package io.dungeons.cli.screen

import com.varabyte.kotter.foundation.LiveVar
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.RunScope
import com.varabyte.kotter.runtime.Session
import io.dungeons.cli.GameStateHolder
import io.dungeons.domain.narrator.NarrateRoomQuery
import io.dungeons.domain.narrator.NarratedRoom
import io.dungeons.port.Id
import org.springframework.stereotype.Component

@Component
class RoomScreen(private val narrateRoomQuery: NarrateRoomQuery, private val gameStateHolder: GameStateHolder) :
    Screen<ScreenTransition>(
        ownTransition = ScreenTransition.Room,
        defaultTransition = ScreenTransition.Exit,
    ) {
    private var room: LiveVar<NarratedRoom> by InitOnce()

    override val sectionBlock: MainRenderScope.() -> Unit
        get() = {
            textLine(room.value.readOut)
            textLine()
            textLine("What do you want to do?")
            textLine()
            for (hero in room.value.party.heroes) {
                text("[${hero.name}] ")
            }
        }

    override val runBlock: RunScope.() -> Unit
        get() = {}

    override fun init(session: Session) {
        val (playerId, gameId) = gameStateHolder.unpackIdsOrThrow()
        val narratedRoom = narrateRoomQuery.query(
            Id.fromUUID(playerId.toUUID()),
            gameId,
        )
        if (narratedRoom == null) {
            error("Cannot load room: save game or adventure not found")
        }
        room = session.liveVarOf(narratedRoom)
    }
}
