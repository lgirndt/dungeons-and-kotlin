package io.dungeons.core

import java.util.*

@JvmInline
value class Id<T>(val value: UUID) {

    companion object {
        fun <T> generate(): Id<T> {
            return Id(UUID.randomUUID())
        }
    }
}