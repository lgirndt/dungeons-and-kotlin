package combat
import TestId
import aPlayerCharacter
import com.google.common.collect.ImmutableListMultimap
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import io.dungeons.*
import io.dungeons.combat.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertThrows

class FactionRelationsTest {

    val FACTION_A = _root_ide_package_.io.dungeons.combat.Faction(name = "Faction A")
    val FACTION_B = _root_ide_package_.io.dungeons.combat.Faction(name = "Faction B")
    val SOME_RELATIONSHIP = _root_ide_package_.io.dungeons.combat.FactionRelationship(
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

fun combatantsBuilder() : ImmutableListMultimap.Builder<Faction, CoreEntity> =
    ImmutableListMultimap.builder<Faction, CoreEntity>()

class CombatantsStoreTest {

    val FACTION_A = _root_ide_package_.io.dungeons.combat.Faction(name = "Faction A")
    val FACTION_B = _root_ide_package_.io.dungeons.combat.Faction(name = "Faction B")

    lateinit var ID : TestId<CoreEntity>
    lateinit var store : CombatantsStore

    @BeforeEach
    fun setup(){
        ID = TestId()
        store = _root_ide_package_.io.dungeons.combat.CombatantsStore(
            combatantsBuilder()
                .putAll(
                    FACTION_A,
                    PlayerCharacter.Companion.aPlayerCharacter(ID[0], name = "Alpha"),
                    PlayerCharacter.aPlayerCharacter(ID[1]),
                    PlayerCharacter.aPlayerCharacter(ID[2])
                )
                .putAll(
                    FACTION_B,
                    PlayerCharacter.aPlayerCharacter(ID[3]),
                    PlayerCharacter.aPlayerCharacter(ID[4]),
                    PlayerCharacter.aPlayerCharacter(ID[5])
                )
                .build(),
        )
    }

    @Test
    fun `find an existing combantant`() {
        val found = store.findOrNull(ID[0])
        assertNotNull(found)
        assertThat(found.entity.name, equalTo("Alpha"))
    }

    @Test
    fun `do not find a non-existing combantant`() {
        val found = store.findOrNull(ID[42])
        assertThat(found, equalTo(null))
    }

    @Test
    fun `findAllWithStance should return correct combatants`() {
        val friendlyToA = store.findAllWithStance(ID[0], FactionStance.Friendly)
        assertThat(
            friendlyToA.map{it.entity.id}.toSet(),
            hasSize(equalTo(3))
            and equalTo(setOf(ID[0], ID[1], ID[2]))
        )

        val hostileToA = store.findAllWithStance(ID[0], FactionStance.Hostile)
        assertThat(
            hostileToA.map{it.entity.id}.toSet(),
            hasSize(equalTo(3))
            and equalTo(setOf(ID[3], ID[4], ID[5]))
        )
    }
}