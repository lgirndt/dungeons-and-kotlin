package io.dungeons.domain.adventure

import io.dungeons.port.AdventureId
import java.util.*

interface AdventureRepository {
    fun findAll(): List<Adventure>

    fun findById(id: AdventureId): Optional<Adventure>

    fun save(entity: Adventure): Adventure?
}
