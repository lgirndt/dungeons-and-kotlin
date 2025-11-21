package combat
import TestId
import aPlayerCharacter
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import fromModifiers
import io.dungeons.Creature
import io.dungeons.Die.Companion.D20
import io.dungeons.PlayerCharacter
import io.dungeons.StatBlock
import io.dungeons.combat.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows
import rolls
import withFixedDice

val FACTION_A = Faction(name = "Faction A")
val FACTION_B = Faction(name = "Faction B")

val SOME_COMBATANT = Combatant(
    creature = PlayerCharacter.aPlayerCharacter(name = "Some Combatant"),
    faction = FACTION_A,
    actor = NoopTurnActor()
)

class FactionRelationsTest {

    val SOME_RELATIONSHIP = FactionRelationship(
        FACTION_A,
        FACTION_B,
        FactionStance.Friendly
    )

    @Test
    fun `queryStance should return an added Relationship`() {
        val relations = FactionRelations.Builder()
            .add(
                FactionRelationship(
                    FACTION_A,
                    FACTION_B,
                    FactionStance.Neutral
                )
            )
            .build()

        assertThat(relations.queryStance(FACTION_A, FACTION_B), equalTo(FactionStance.Neutral))
        assertThat(relations.queryStance(FACTION_B, FACTION_A), equalTo(FactionStance.Neutral))
    }

    @Test
    fun `queryStance should return Hostile for unadded Relationship`() {
        val relations = FactionRelations.Builder().build()
        assertThat(relations.queryStance(FACTION_A, FACTION_B), equalTo(FactionStance.Hostile))
        assertThat(relations.queryStance(FACTION_B, FACTION_A), equalTo(FactionStance.Hostile))
    }

    @Test
    fun `An added hostile Relationship should be queried as hostile`() {
        val relations = FactionRelations.Builder()
            .add(
                FactionRelationship(
                    FACTION_A,
                    FACTION_B,
                    FactionStance.Hostile
                )
            )
            .build()

        assertThat(relations.queryStance(FACTION_A, FACTION_B), equalTo(FactionStance.Hostile))
        assertThat(relations.queryStance(FACTION_B, FACTION_A), equalTo(FactionStance.Hostile))
    }

    @Test
    fun `adding an existing Relationship should throw an exception`() {
        assertThrows<IllegalArgumentException> {
            FactionRelations.Builder()
                .add(SOME_RELATIONSHIP)
                .add(SOME_RELATIONSHIP)
                .build()
        }
    }

    @Test
    fun `queryStance for the same faction should be Friendly`() {
        val relations = FactionRelations.Builder().build()
        assertThat(relations.queryStance(FACTION_A, FACTION_A), equalTo(FactionStance.Friendly))
    }
}

class CombatantsStoreTest {

    val FACTION_A = Faction(name = "Faction A")
    val FACTION_B = Faction(name = "Faction B")

    lateinit var ID : TestId<Creature>
    lateinit var store : CombatantsStore

    @BeforeEach
    fun setup(){
        ID = TestId()
        store = CombatantsStore(
            listOf(
                Combatant(PlayerCharacter.Companion.aPlayerCharacter(ID[0], name = "Alpha"), FACTION_A, NoopTurnActor()),
                Combatant(PlayerCharacter.aPlayerCharacter(ID[1]), FACTION_A, NoopTurnActor()),
                Combatant(PlayerCharacter.aPlayerCharacter(ID[2]), FACTION_A, NoopTurnActor()),
                Combatant(PlayerCharacter.aPlayerCharacter(ID[3]), FACTION_B, NoopTurnActor()),
                Combatant(PlayerCharacter.aPlayerCharacter(ID[4]), FACTION_B, NoopTurnActor()),
                Combatant(PlayerCharacter.aPlayerCharacter(ID[5]), FACTION_B, NoopTurnActor()),
            )
        )
    }

    @Test
    fun `find an existing combantant`() {
        val found = store[ID[0]]
        assertNotNull(found)
        assertThat(found.creature.name, equalTo("Alpha"))
    }

    @Test
    fun `do not find a non-existing combantant`() {
        val found = store[ID[42]]
        assertThat(found, equalTo(null))
    }

    @Test
    fun `findAllWithStance should return correct combatants`() {
        val friendlyToA = store.findAllWithStance(ID[0], FactionStance.Friendly)
        assertThat(
            friendlyToA.map{it.creature.id}.toSet(),
            hasSize(equalTo(3))
            and equalTo(setOf(ID[0], ID[1], ID[2]))
        )

        val hostileToA = store.findAllWithStance(ID[0], FactionStance.Hostile)
        assertThat(
            hostileToA.map{it.creature.id}.toSet(),
            hasSize(equalTo(3))
            and equalTo(setOf(ID[3], ID[4], ID[5]))
        )
    }
}

class CombatantTest {

    @Test
    fun `initiative should be cached after first access`() {
        val creature = PlayerCharacter.aPlayerCharacter(name = "Test Character")
        val combatant = SOME_COMBATANT.copy()

        withFixedDice(D20 rolls 12) {
            // Access initiative twice
            val firstRoll = combatant.initiative
            val secondRoll = combatant.initiative

            // Should be the exact same instance (lazy evaluation caches the result)
            assertThat(firstRoll, equalTo(secondRoll))
        }
    }

    @Test
    fun `initiative should include dexterity modifier`() {

        val player = PlayerCharacter.aPlayerCharacter(
            name = "Dexterous Character",
            stats = StatBlock.fromModifiers(dexMod=4)
        )
        withFixedDice(D20 rolls 12) {
            val combatant = SOME_COMBATANT.copy(creature = player)
            val initiative = combatant.initiative
            assertThat(initiative.value, equalTo(12+4))
            assertThat(initiative.die, equalTo(D20))
        }
    }
}