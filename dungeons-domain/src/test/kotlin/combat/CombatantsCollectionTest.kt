package combat

import TestId
import aPlayerCharacter
import com.natpryce.hamkrest.and
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import io.dungeons.Creature
import io.dungeons.PlayerCharacter
import io.dungeons.combat.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull

class CombatantsCollectionTest {

    val FACTION_A = Faction(name = "Faction A")
    val FACTION_B = Faction(name = "Faction B")

    lateinit var ID : TestId<Creature>
    lateinit var store : CombatantsCollection

    @BeforeEach
    fun setup(){
        ID = TestId()
        store = CombatantsCollection(
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
    fun `get an existing combatant`() {
        val found = store[ID[0]]
        assertNotNull(found)
        assertThat(found.creature.name, equalTo("Alpha"))
    }

    @Test
    fun `do not get a non-existing combantant`() {
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
