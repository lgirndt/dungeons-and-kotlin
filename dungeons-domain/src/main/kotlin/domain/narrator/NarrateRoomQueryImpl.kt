package io.dungeons.domain.narrator

import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.domain.adventure.RoomRepository
import io.dungeons.domain.savegame.SaveGameRepository
import io.dungeons.domain.world.Room
import io.dungeons.port.HeroResponse
import io.dungeons.port.LeaveRoomAction
import io.dungeons.port.NarrateRoomQuery
import io.dungeons.port.NarratedRoomResponse
import io.dungeons.port.PartyResponse
import io.dungeons.port.PlayerId
import io.dungeons.port.RoomAction
import io.dungeons.port.SaveGameId
import io.dungeons.port.usecases.UseCaseException
import org.springframework.stereotype.Component
import kotlin.jvm.optionals.getOrNull

private fun Hero.toResponse() = HeroResponse(name = this.name)

private fun Party.toResponse() = PartyResponse(heroes = this.heroes.map(Hero::toResponse))

private fun NarratedRoom.toResponse() = NarratedRoomResponse(
    roomId = this.roomId.toUUID(),
    readOut = this.readOut,
    party = this.party.toResponse(),
    availableActions = this.availableActions,
)

class NarrateRoomException(message: String) : UseCaseException(message)

@Component
class NarrateRoomQueryImpl(
    private val saveGameRepository: SaveGameRepository,
    private val adventureRepository: AdventureRepository,
    private val roomRepository: RoomRepository,
) : NarrateRoomQuery {
    override fun query(playerId: PlayerId, saveGameId: SaveGameId): Result<NarratedRoomResponse> {
        val saveGameIdTyped = saveGameId
        val saveGame = saveGameRepository.findByUserId(playerId, saveGameIdTyped).getOrNull()
            ?: return Result.failure(
                NarrateRoomException("Cannot find save game with id $saveGameId for player $playerId"),
            )

//        val adventure = adventureRepository.findById(saveGame.adventureId).getOrNull()
//            ?: return Result.failure(NarrateRoomException("Cannot find adventure with id ${saveGame.adventureId}"))

        val currentRoom = roomRepository.find(saveGame.adventureId, saveGame.currentRoomId).getOrNull()
            ?: return Result.failure(NarrateRoomException("Cannot find room with id ${saveGame.currentRoomId}"))

        // TODO we need to load our game properly
        val narratedRoom = NarratedRoom(
            roomId = saveGame.currentRoomId,
            party = Party(
                heroes = listOf(
                    Hero(name = "Aragorn"),
                    Hero(name = "Legolas"),
                ),
            ),
            readOut = currentRoom.description,
            availableActions = createLeaveActions(currentRoom),
        )
        return narratedRoom.toResponse().let { Result.success(it) }
    }

    private fun createLeaveActions(currentRoom: Room): List<RoomAction> = currentRoom.doors.map { door ->
        LeaveRoomAction(
            targetDoorId = door.id,
            description = "The door in the ${door.direction}",
        )
    }
}
