
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.example.Faction
import org.example.FactionRelations
import org.example.FactionRelationship
import org.example.FactionStance
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FactionRelationsTest {

    val FACTION_A = Faction(name="Faction A")
    val FACTION_B = Faction(name="Faction B")
    val SOME_RELATIONSHIP = FactionRelationship(FACTION_A, FACTION_B, FactionStance.Friendly)

    @Test
    fun `queryStance should return an added Relationship`() {
        val relations = FactionRelations().apply {
            add(FactionRelationship(FACTION_A, FACTION_B, FactionStance.Neutral))
        }

        assertThat(relations.queryStance(FACTION_A, FACTION_B), equalTo(FactionStance.Neutral))
        assertThat(relations.queryStance(FACTION_B, FACTION_A), equalTo(FactionStance.Neutral))
    }

    @Test
    fun `queryStance should return Hostile for unadded Relationship`() {
        val relations = FactionRelations()
        assertThat(relations.queryStance(FACTION_A, FACTION_B), equalTo(FactionStance.Hostile))
        assertThat(relations.queryStance(FACTION_B, FACTION_A), equalTo(FactionStance.Hostile))
    }

    @Test
    fun `An added hostile Relationship should be queried as hostile`() {
        val relations = FactionRelations().apply {
            add(FactionRelationship(FACTION_A, FACTION_B, FactionStance.Hostile))
        }

        assertThat(relations.queryStance(FACTION_A, FACTION_B), equalTo(FactionStance.Hostile))
        assertThat(relations.queryStance(FACTION_B, FACTION_A), equalTo(FactionStance.Hostile))
    }

    @Test
    fun `adding an existing Relationship should throw an exception`() {
        val relations = FactionRelations().apply {
            add(SOME_RELATIONSHIP)
        }

        assertThrows<IllegalArgumentException> {
            relations.add(SOME_RELATIONSHIP)
        }
    }

    @Test
    fun `queryStance for the same faction should be Friendly`() {
        val relations = FactionRelations()
        assertThat(relations.queryStance(FACTION_A, FACTION_A), equalTo(FactionStance.Friendly))
    }
}