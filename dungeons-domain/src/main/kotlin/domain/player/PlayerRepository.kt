package io.dungeons.domain.player

import java.util.*

interface PlayerRepository {
    fun insert(player: Player): Player

    fun findByName(name: String): Optional<Player>
}
