package io.dungeons.domain.adventure


import io.dungeons.domain.core.Id

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


    override fun listAdventures(): List<Adventure> = adventures

    override fun find(id: Id<Adventure>): Adventure? = adventures.find { it.id == id }
}
