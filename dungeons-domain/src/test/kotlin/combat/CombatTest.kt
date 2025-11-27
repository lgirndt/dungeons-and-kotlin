package combat

import TestId
import aPlayerCharacter
import fromModifiers
import io.dungeons.Creature
import io.dungeons.Die.Companion.D20
import io.dungeons.StatBlock
import io.dungeons.combat.Combatant
import io.dungeons.combat.CombatantsCollection
import io.dungeons.combat.Faction
import io.dungeons.combat.FactionRelations
import io.dungeons.combat.FactionRelationship
import io.dungeons.combat.FactionStance
import io.dungeons.combat.NoopTurnActor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import rolls
import withFixedDice

private val FACTION_A = Faction(name = "Faction A")
private val FACTION_B = Faction(name = "Faction B")

private val SOME_COMBATANT = Combatant(
    creature = aPlayerCharacter(name = "Some Combatant"),
    faction = FACTION_A,
    actor = NoopTurnActor(),
)

private val SOME_RELATIONSHIP = FactionRelationship(
    FACTION_A,
    FACTION_B,
    FactionStance.Friendly,
)

private val ID: TestId<Creature> = TestId()

class FactionRelationsTest {
    @Test
    fun `queryStance should return an added Relationship`() {
        val relations = FactionRelations
            .Builder()
            .add(
                FactionRelationship(
                    FACTION_A,
                    FACTION_B,
                    FactionStance.Neutral,
                ),
            )
            .build()

        assertEquals(FactionStance.Neutral, relations.queryStance(FACTION_A, FACTION_B))
        assertEquals(FactionStance.Neutral, relations.queryStance(FACTION_B, FACTION_A))
    }

    @Test
    fun `queryStance should return Hostile for unadded Relationship`() {
        val relations = FactionRelations.Builder().build()
        assertEquals(FactionStance.Hostile, relations.queryStance(FACTION_A, FACTION_B))
        assertEquals(FactionStance.Hostile, relations.queryStance(FACTION_B, FACTION_A))
    }

    @Test
    fun `An added hostile Relationship should be queried as hostile`() {
        val relations = FactionRelations
            .Builder()
            .add(
                FactionRelationship(
                    FACTION_A,
                    FACTION_B,
                    FactionStance.Hostile,
                ),
            )
            .build()

        assertEquals(FactionStance.Hostile, relations.queryStance(FACTION_A, FACTION_B))
        assertEquals(FactionStance.Hostile, relations.queryStance(FACTION_B, FACTION_A))
    }

    @Test
    fun `adding an existing Relationship should throw an exception`() {
        assertThrows<IllegalArgumentException> {
            FactionRelations
                .Builder()
                .add(SOME_RELATIONSHIP)
                .add(SOME_RELATIONSHIP)
                .build()
        }
    }

    @Test
    fun `queryStance for the same faction should be Friendly`() {
        val relations = FactionRelations.Builder().build()
        assertEquals(FactionStance.Friendly, relations.queryStance(FACTION_A, FACTION_A))
    }
}

class CombatantsStoreTest {
    val factionA = Faction(name = "Faction A")
    val factionB = Faction(name = "Faction B")

    lateinit var store: CombatantsCollection

    @BeforeEach
    fun setup() {
        store = CombatantsCollection(
            listOf(
                Combatant(aPlayerCharacter(ID[0], name = "Alpha"), factionA, NoopTurnActor()),
                Combatant(aPlayerCharacter(ID[1]), factionA, NoopTurnActor()),
                Combatant(aPlayerCharacter(ID[2]), factionA, NoopTurnActor()),
                Combatant(aPlayerCharacter(ID[3]), factionB, NoopTurnActor()),
                Combatant(aPlayerCharacter(ID[4]), factionB, NoopTurnActor()),
                Combatant(aPlayerCharacter(ID[5]), factionB, NoopTurnActor()),
            ),
        )
    }

    @Test
    fun `find an existing combantant`() {
        val found = store[ID[0]]
        assertNotNull(found)
        assertEquals("Alpha", found.creature.name)
    }

    @Test
    fun `do not find a non-existing combantant`() {
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

class CombatantTest {
    @Test
    fun `initiative should be cached after first access`() {
        val creature = aPlayerCharacter(name = "Test Character")
        val combatant = SOME_COMBATANT.copy()

        withFixedDice(D20 rolls 12) {
            // Access initiative twice
            val firstRoll = combatant.initiative
            val secondRoll = combatant.initiative

            // Should be the exact same instance (lazy evaluation caches the result)
            assertEquals(secondRoll, firstRoll)
        }
    }

    @Test
    fun `initiative should include dexterity modifier`() {
        val player = aPlayerCharacter(
            name = "Dexterous Character",
            stats = StatBlock.fromModifiers(dexMod = 4),
        )
        withFixedDice(D20 rolls 12) {
            val combatant = SOME_COMBATANT.copy(creature = player)
            val initiative = combatant.initiative
            assertEquals(12 + 4, initiative.value)
            assertEquals(D20, initiative.die)
        }
    }
}
