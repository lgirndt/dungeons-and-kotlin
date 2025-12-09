package io.dungeons.domain.core

import java.util.*

@JvmInline
value class Id<T>(val value: UUID) {
    fun toUUID(): UUID = value

    fun asStringRepresentation(): String = value.toString()

    companion object {
        fun <T> generate(): Id<T> = Id(UUID.randomUUID())

        fun <T> fromUUID(uuid: UUID): Id<T> = Id(uuid)

        fun <T> fromString(idString: String): Id<T> = Id(UUID.fromString(idString))
    }
}
