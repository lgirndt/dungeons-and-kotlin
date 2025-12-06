package io.dungeons.domain.adventure


import io.dungeons.domain.core.Id
import java.util.logging.Logger.getLogger

class MockAdventureRepository : AdventureRepository {

    private val logger = getLogger(MockAdventureRepository::class.java.name)

    override fun listAdventures(): List<Adventure> {
        logger.info("Listing mock adventures")
        return listOf(
            Adventure(
                id=Id.generate(),
                name="The Lost City",
                initialRoomId = Id.generate()
            ),
            Adventure(
                id=Id.generate(),
                name="Dragon's Lair",
                initialRoomId = Id.generate()
            ),
            Adventure(
                id=Id.generate(),
                name="Cursed Forest",
                initialRoomId = Id.generate()
            ),
        )
    }
}
