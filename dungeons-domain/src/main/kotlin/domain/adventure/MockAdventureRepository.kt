package io.dungeons.domain.adventure

import io.dungeons.domain.core.Id
import java.util.*

class MockAdventureRepository : AdventureRepository {
    private val adventures = mutableListOf(
        Adventure(
            id = Id.generate(),
            name = "The Lost City",
            initialRoomId = Id.generate(),
            emptyList(),
        ),
        Adventure(
            id = Id.generate(),
            name = "Dragon's Lair",
            initialRoomId = Id.generate(),
            emptyList(),
        ),
        Adventure(
            id = Id.generate(),
            name = "Cursed Forest",
            initialRoomId = Id.generate(),
            emptyList(),
        ),
    )

    override fun findAll(): List<Adventure> = adventures

    override fun findById(id: Id<Adventure>): Optional<Adventure> = Optional.ofNullable(adventures.find { it.id == id })

    override fun save(entity: Adventure): Adventure {
        val existingIndex = adventures.indexOfFirst { it.id == entity.id }
        if (existingIndex >= 0) {
            adventures[existingIndex] = entity
        } else {
            adventures.add(entity)
        }
        return entity
    }
}
