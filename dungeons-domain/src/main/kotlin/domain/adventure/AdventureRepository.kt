package io.dungeons.domain.adventure

import io.dungeons.domain.core.Id

interface AdventureRepository {
    fun findAll(): List<Adventure>
    fun findByIdOrNull(id: Id<Adventure>): Adventure?
}