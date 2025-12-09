package io.dungeons.domain.adventure

import io.dungeons.domain.core.Id
import java.util.*

class MockAdventureRepository : AdventureRepository {
    private val adventures = listOf(
        Adventure(
            id = Id.generate(),
            name = "The Lost City",
            initialRoomId = Id.generate(),
        ),
        Adventure(
            id = Id.generate(),
            name = "Dragon's Lair",
            initialRoomId = Id.generate(),
        ),
        Adventure(
            id = Id.generate(),
            name = "Cursed Forest",
            initialRoomId = Id.generate(),
        ),
    )

    override fun findAll(): List<Adventure> = adventures

    override fun findById(id: Id<Adventure>): Optional<Adventure> = Optional.ofNullable(adventures.find { it.id == id })
}
