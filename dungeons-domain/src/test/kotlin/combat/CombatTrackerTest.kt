package combat

import aPlayerCharacter
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import fromModifiers
import io.dungeons.Die.Companion.D20
import io.dungeons.PlayerCharacter
import io.dungeons.StatBlock
import io.dungeons.combat.Combatant
import io.dungeons.combat.CombatTracker
import io.dungeons.combat.CombatTrackerListener
import io.dungeons.combat.Faction
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import rolls
import withFixedDice

class CombatTrackerTest {

    @Test
    fun `combatants should be sorted by initiative in descending order`() {
        val lowDexPlayer = PlayerCharacter.aPlayerCharacter(
            name = "Slow Character",
            stats = StatBlock.fromModifiers(dexMod = 0)
        )
        val midDexPlayer = PlayerCharacter.aPlayerCharacter(
            name = "Medium Character",
            stats = StatBlock.fromModifiers(dexMod = 2)
        )
        val highDexPlayer = PlayerCharacter.aPlayerCharacter(
            name = "Fast Character",
            stats = StatBlock.fromModifiers(dexMod = 4)
        )

        val faction = Faction(name = "Heroes")

        withFixedDice(
            D20 rolls 10,  // lowDexPlayer: 10 + 0 = 10
            D20 rolls 12,  // midDexPlayer: 12 + 2 = 14
            D20 rolls 8    // highDexPlayer: 8 + 4 = 12
        ) {
            val combatants = listOf(
                Combatant(entity = lowDexPlayer, faction = faction),
                Combatant(entity = midDexPlayer, faction = faction),
                Combatant(entity = highDexPlayer, faction = faction)
            )

            val tracker = CombatTracker(combatants)

            // Verify the combatants are sorted by initiative (descending)
            // Expected order: midDexPlayer (14), highDexPlayer (12), lowDexPlayer (10)
            assertThat(tracker.combatants.size, equalTo(3))
            assertThat(tracker.combatants[0].entity.name, equalTo("Medium Character"))
            assertThat(tracker.combatants[0].initiative.value, equalTo(14))
            assertThat(tracker.combatants[1].entity.name, equalTo("Fast Character"))
            assertThat(tracker.combatants[1].initiative.value, equalTo(12))
            assertThat(tracker.combatants[2].entity.name, equalTo("Slow Character"))
            assertThat(tracker.combatants[2].initiative.value, equalTo(10))
        }
    }

    @Test
    fun `combatants with same initiative should maintain input order`() {
        val player1 = PlayerCharacter.aPlayerCharacter(
            name = "First",
            stats = StatBlock.fromModifiers(dexMod = 2)
        )
        val player2 = PlayerCharacter.aPlayerCharacter(
            name = "Second",
            stats = StatBlock.fromModifiers(dexMod = 2)
        )

        val faction = Faction(name = "Party")

        withFixedDice(
            D20 rolls 10,  // player1: 10 + 2 = 12
            D20 rolls 10   // player2: 10 + 2 = 12
        ) {
            val combatants = listOf(
                Combatant(entity = player1, faction = faction),
                Combatant(entity = player2, faction = faction)
            )

            val tracker = CombatTracker(combatants)

            // Both have initiative 12, should maintain input order
            assertThat(tracker.combatants[0].entity.name, equalTo("First"))
            assertThat(tracker.combatants[1].entity.name, equalTo("Second"))
        }
    }

    @Test
    fun `empty combatants list should create empty tracker`() {
        val tracker = CombatTracker(emptyList())
        assertThat(tracker.combatants.size, equalTo(0))
    }

    @Test
    fun `single combatant should be first in order`() {
        val player = PlayerCharacter.aPlayerCharacter(
            name = "Solo",
            stats = StatBlock.fromModifiers(dexMod = 1)
        )
        val faction = Faction(name = "Lone Wolf")

        withFixedDice(D20 rolls 15) {
            val combatant = Combatant(entity = player, faction = faction)
            val tracker = CombatTracker(listOf(combatant))

            assertThat(tracker.combatants.size, equalTo(1))
            assertThat(tracker.combatants[0].entity.name, equalTo("Solo"))
            assertThat(tracker.combatants[0].initiative.value, equalTo(15 + 1))
        }
    }

    @Test
    fun `listener should be called with sorted combatants after initiative is rolled`() {
        val player1 = PlayerCharacter.aPlayerCharacter(
            name = "First",
            stats = StatBlock.fromModifiers(dexMod = 1)
        )
        val player2 = PlayerCharacter.aPlayerCharacter(
            name = "Second",
            stats = StatBlock.fromModifiers(dexMod = 2)
        )

        val faction = Faction(name = "Party")
        val listener = mockk<CombatTrackerListener>(relaxed = true)

        withFixedDice(
            D20 rolls 10,  // player1: 10 + 1 = 11
            D20 rolls 8    // player2: 8 + 2 = 10
        ) {
            val combatants = listOf(
                Combatant(entity = player1, faction = faction),
                Combatant(entity = player2, faction = faction)
            )

            CombatTracker(combatants, listener = listener)

            // Verify listener was called with the sorted list
            verify(exactly = 1) {
                listener.afterRolledInitiative(match { sortedCombatants ->
                    sortedCombatants.size == 2 &&
                    sortedCombatants[0].entity.name == "First" &&
                    sortedCombatants[0].initiative.value == 11 &&
                    sortedCombatants[1].entity.name == "Second" &&
                    sortedCombatants[1].initiative.value == 10
                })
            }
        }
    }
}