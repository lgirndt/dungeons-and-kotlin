@file:Suppress("ClassNaming")

package io.dungeons.port

import java.util.*

@JvmInline
value class Id<T>(val value: UUID) {
    fun toUUID(): UUID = value

    fun asStringRepresentation(): String = value.toString()

    fun <S> castTo(): Id<S> = Id(value)

    companion object {
        fun <T> generate(): Id<T> = Id(UUID.randomUUID())

        fun <T> fromUUID(uuid: UUID): Id<T> = Id(uuid)

        fun <T> fromString(idString: String): Id<T> = Id(UUID.fromString(idString))
    }
}

object _Player

object _SaveGame

object _Creature

object _Adventure

object _Room

object _World

object _WorldState

object _Faction

object _Token

typealias PlayerId = Id<_Player>
typealias SaveGameId = Id<_SaveGame>
typealias CreatureId = Id<_Creature>
typealias AdventureId = Id<_Adventure>
typealias RoomId = Id<_Room>
typealias WorldId = Id<_World>
typealias WorldStateId = Id<_WorldState>
typealias FactionId = Id<_Faction>
typealias TokenId = Id<_Token>
