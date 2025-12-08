package io.dungeons.domain.adventure

import io.dungeons.domain.core.Id

interface AdventureRepository {
    fun listAdventures(): List<Adventure>
    fun find(id: Id<Adventure>): Adventure?
}