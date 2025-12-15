package io.dungeons.port

import java.util.*

@JvmInline
value class Id<T>(val value: UUID) {
    fun toUUID(): UUID = value

    fun asStringRepresentation(): String = value.toString()

    fun <S> castTo() : Id<S> = Id(value)

    companion object {
        fun <T> generate(): Id<T> = Id(UUID.randomUUID())

        fun <T> fromUUID(uuid: UUID): Id<T> = Id(uuid)

        fun <T> fromString(idString: String): Id<T> = Id(UUID.fromString(idString))
    }
}

@Suppress("ClassNaming")
object _Player

@Suppress("ClassNaming")
object _SaveGame

typealias PlayerId = Id<_Player>
typealias SaveGameId = Id<_SaveGame>
