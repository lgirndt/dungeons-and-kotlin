package cli.screen

import cli.GameStateHolder
import cli.Player
import com.varabyte.kotter.foundation.LiveVar
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import com.varabyte.kotter.runtime.RunScope
import com.varabyte.kotter.runtime.Session
import io.dungeons.domain.core.Id
import io.dungeons.domain.narrator.NarrateRoomQuery
import io.dungeons.domain.narrator.NarratedRoom
import io.dungeons.domain.savegame.SaveGame
import org.springframework.stereotype.Component

@Component
class RoomScreen(
    private val narrateRoomQuery: NarrateRoomQuery,
    private val gameStateHolder: GameStateHolder,
) : Screen<ScreenTransition>(
    ownTransition = ScreenTransition.Room,
    defaultTransition = ScreenTransition.Exit,
) {
    private lateinit var room : LiveVar<NarratedRoom>

    override fun init(session: Session) {
        val (playerId: Id<Player>, gameId: Id<SaveGame>) = gameStateHolder.unpackIdsOrThrow()
        val narratedRoom = narrateRoomQuery.execute(
            Id.fromUUID(playerId.toUUID()),
            gameId
        )
        require(narratedRoom != null)
        room = session.liveVarOf(narratedRoom)
    }

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

}