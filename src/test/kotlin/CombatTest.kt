
import com.google.common.collect.ImmutableListMultimap
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.example.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows

class FactionRelationsTest {

    val FACTION_A = Faction(name="Faction A")
    val FACTION_B = Faction(name="Faction B")
    val SOME_RELATIONSHIP = FactionRelationship(FACTION_A, FACTION_B, FactionStance.Friendly)

    @Test
    fun `queryStance should return an added Relationship`() {
        val relations = FactionRelations.Builder()
            .add(FactionRelationship(FACTION_A, FACTION_B, FactionStance.Neutral))
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
            .add(FactionRelationship(FACTION_A, FACTION_B, FactionStance.Hostile))
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

fun CombatantsBuilder() : ImmutableListMultimap.Builder<Faction, CoreEntity> =
    ImmutableListMultimap.builder<Faction, CoreEntity>()

class CombatantsStoreTest {

    val FACTION_A = Faction(name="Faction A")
    val FACTION_B = Faction(name="Faction B")

    val ID = (0..5).map { Id.generate<CoreEntity>() }

    @Test
    fun `construct a CombatantsStore`() {
        // Just a construction test for now
        val store = CombatantsStore(
            CombatantsBuilder()
                .putAll(FACTION_A,
                    PlayerCharacter.aPlayerCharacter(ID[0], name="Alpha"),
                    PlayerCharacter.aPlayerCharacter(ID[1]),
                    PlayerCharacter.aPlayerCharacter(ID[2])
                )
                .putAll(FACTION_B,
                    PlayerCharacter.aPlayerCharacter(ID[3]),
                    PlayerCharacter.aPlayerCharacter(ID[4]),
                    PlayerCharacter.aPlayerCharacter(ID[5])
                )
                .build(),
        )
        val found = store.find(ID[0])
        assertNotNull(found)
        assertThat(found.entity.name, equalTo("Alpha"))
    }
}