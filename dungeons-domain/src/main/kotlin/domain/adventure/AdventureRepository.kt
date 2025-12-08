package io.dungeons.domain.adventure

import io.dungeons.domain.core.Id
import java.util.*

interface AdventureRepository {
    fun findAll(): List<Adventure>
    fun findById(id: Id<Adventure>): Optional<Adventure>
}