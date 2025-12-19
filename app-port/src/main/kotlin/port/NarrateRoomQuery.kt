package io.dungeons.port

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeName
import java.util.*

data class HeroResponse(val name: String)

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
sealed interface RoomAction

@JsonTypeName("move")
data class LeaveRoomAction(val targetDoorId: DoorId, val description: String) : RoomAction

@JsonTypeName("investigate")
data class InvestigateAction(val targetId: String, val targetName: String, val description: String) : RoomAction

@JsonTypeName("talk")
data class TalkAction(val npcId: UUID, val npcName: String, val greeting: String) : RoomAction

data class PartyResponse(val heroes: List<HeroResponse>)

data class NarratedRoomResponse(
    val roomId: UUID,
    val readOut: String,
    val party: PartyResponse,
    val availableActions: List<RoomAction>,
)

interface NarrateRoomQuery {
    fun query(playerId: PlayerId, saveGameId: SaveGameId): Result<NarratedRoomResponse>
}
