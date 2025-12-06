package io.dungeons.domain.adventure

interface AdventureRepository {
    fun listAdventures(): List<Adventure>
}