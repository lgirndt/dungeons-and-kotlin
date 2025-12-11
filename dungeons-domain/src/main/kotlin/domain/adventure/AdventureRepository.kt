package io.dungeons.domain.adventure

import io.dungeons.port.Id
import java.util.*

interface AdventureRepository {
    fun findAll(): List<Adventure>

    fun findById(id: Id<Adventure>): Optional<Adventure>

    fun save(entity: Adventure): Adventure?
}
