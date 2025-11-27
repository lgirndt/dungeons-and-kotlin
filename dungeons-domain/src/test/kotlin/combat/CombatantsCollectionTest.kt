package combat

import TestId
import aPlayerCharacter
import io.dungeons.Creature
import io.dungeons.PlayerCharacter
import io.dungeons.combat.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull

class CombatantsCollectionTest {
    val FACTION_A = Faction(name = "Faction A")
    val FACTION_B = Faction(name = "Faction B")

    lateinit var ID: TestId<Creature>
    lateinit var store: CombatantsCollection

    @BeforeEach
    fun setup() {
        ID = TestId()
        store = CombatantsCollection(
            listOf(
                Combatant(aPlayerCharacter(ID[0], name = "Alpha"), FACTION_A, NoopTurnActor()),
                Combatant(aPlayerCharacter(ID[1]), FACTION_A, NoopTurnActor()),
                Combatant(aPlayerCharacter(ID[2]), FACTION_A, NoopTurnActor()),
                Combatant(aPlayerCharacter(ID[3]), FACTION_B, NoopTurnActor()),
                Combatant(aPlayerCharacter(ID[4]), FACTION_B, NoopTurnActor()),
                Combatant(aPlayerCharacter(ID[5]), FACTION_B, NoopTurnActor()),
            )
        )
    }

    @Test
    fun `get an existing combatant`() {
        val found = store[ID[0]]
        assertNotNull(found)
        assertEquals("Alpha", found.creature.name)
    }

    @Test
    fun `do not get a non-existing combantant`() {
        val found = store[ID[42]]
        assertEquals(null, found)
    }

    @Test
    fun `findAllWithStance should return correct combatants`() {
        val friendlyToA = store.findAllWithStance(ID[0], FactionStance.Friendly)
        val friendlyIds = friendlyToA.map { it.creature.id }.toSet()
        assertEquals(3, friendlyIds.size)
        assertEquals(setOf(ID[0], ID[1], ID[2]), friendlyIds)

        val hostileToA = store.findAllWithStance(ID[0], FactionStance.Hostile)
        val hostileIds = hostileToA.map { it.creature.id }.toSet()
        assertEquals(3, hostileIds.size)
        assertEquals(setOf(ID[3], ID[4], ID[5]), hostileIds)
    }
}
