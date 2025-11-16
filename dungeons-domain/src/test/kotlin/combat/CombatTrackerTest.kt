package combat

import aPlayerCharacter
import com.google.common.collect.ImmutableListMultimap
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import fromModifiers
import io.dungeons.CoreEntity
import io.dungeons.Die.Companion.D20
import io.dungeons.DieRoll
import io.dungeons.PlayerCharacter
import io.dungeons.StatBlock
import io.dungeons.combat.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import rolls
import withFixedDice

class CombatTrackerTest {

    val PLAYER_FACTION = Faction(name = "Players")
    val MONSTER_FACTION = Faction(name = "Monsters")

    fun aTrackerEntity(
        name: String = "some name",
        dexMod: Int = 0,
        faction: Faction = PLAYER_FACTION,
        actor: TurnActor = mockk<TurnActor>(relaxed = true)
    ): TrackerEntry {
        return TrackerEntry(
            combatant = Combatant(
                entity = PlayerCharacter.aPlayerCharacter(
                    name = name,
                    stats = StatBlock.fromModifiers(dexMod = dexMod)
                ),
                faction = faction
            ),
            actor = actor
        )
    }

    @Test
    fun `combatants should be sorted by initiative in descending order`() {
        val trackerEntries = listOf(
            aTrackerEntity(name = "Slow Character", dexMod = 0),
            aTrackerEntity(name = "Medium Character", dexMod = 2),
            aTrackerEntity(name = "Fast Character", dexMod = 4),
        )
        withFixedDice(
            D20 rolls 10,  // lowDexPlayer: 10 + 0 = 10
            D20 rolls 12,  // midDexPlayer: 12 + 2 = 14
            D20 rolls 8    // highDexPlayer: 8 + 4 = 12
        ) {

            val tracker = createCombatTracker(trackerEntries)

            // Verify the combatants are sorted by initiative (descending)
            // Expected order: midDexPlayer (14), highDexPlayer (12), lowDexPlayer (10)
            assertThat(tracker.combatantsOrderedByInitiative.size, equalTo(3))
            assertThat(tracker.combatantsOrderedByInitiative[0].entity.name, equalTo("Medium Character"))
            assertThat(tracker.combatantsOrderedByInitiative[0].initiative.value, equalTo(14))
            assertThat(tracker.combatantsOrderedByInitiative[1].entity.name, equalTo("Fast Character"))
            assertThat(tracker.combatantsOrderedByInitiative[1].initiative.value, equalTo(12))
            assertThat(tracker.combatantsOrderedByInitiative[2].entity.name, equalTo("Slow Character"))
            assertThat(tracker.combatantsOrderedByInitiative[2].initiative.value, equalTo(10))
        }
    }

    @Test
    fun `combatants with same initiative should maintain input order`() {
        withFixedDice(
            D20 rolls 10,  // player1: 10 + 2 = 12
            D20 rolls 10   // player2: 10 + 2 = 12
        ) {
            val trackerEntries = listOf(
                aTrackerEntity(name = "First", dexMod = 2),
                aTrackerEntity(name = "Second", dexMod = 2),
            )

            val tracker = createCombatTracker(trackerEntries)

            // Both have initiative 12, should maintain input order
            assertThat(tracker.combatantsOrderedByInitiative[0].entity.name, equalTo("First"))
            assertThat(tracker.combatantsOrderedByInitiative[1].entity.name, equalTo("Second"))
        }
    }

    private fun toCombatScenario(trackerEntries: List<TrackerEntry>): SimpleCombatScenario =
        SimpleCombatScenario(toCombatStore(trackerEntries))

    private fun toCombatStore(trackerEntries: List<TrackerEntry>): CombatantsStore = CombatantsStore(
        combatantsByFaction = ImmutableListMultimap.builder<Faction, CoreEntity>()
            .apply {
                trackerEntries
                    .map(TrackerEntry::combatant)
                    .forEach { combatant ->
                        put(combatant.faction, combatant.entity)
                    }
            }
            .build()
    )

    private fun createCombatTracker(trackerEntries: List<TrackerEntry>): CombatTracker = CombatTracker(
        trackerEntries = trackerEntries,
        combatScenario = toCombatScenario(trackerEntries)
    )

    @Test
    fun `empty combatants list should create empty tracker`() {
        assertThrows<IllegalArgumentException> {
            createCombatTracker(emptyList())
        }
    }

    @Test
    fun `listener should be called with sorted combatants after initiative is rolled`() {

        val listener = mockk<CombatTrackerListener>(relaxed = true)

        val trackerEntries = listOf(
            aTrackerEntity(name = "First", dexMod = 1),
            aTrackerEntity(name = "Second", dexMod = 2),
        )
        withFixedDice(
            D20 rolls 10,  // player1: 10 + 1 = 11
            D20 rolls 8    // player2: 8 + 2 = 10
        ) {

            CombatTracker(
                trackerEntries = trackerEntries,
                combatScenario = toCombatScenario(trackerEntries),
                listener = listener
            )

            // Verify listener was called with the sorted list
            verify(exactly = 1) {
                listener.rolledInitiative(match { sortedCombatants ->
                    sortedCombatants.size == 2 &&
                            sortedCombatants[0].entity.name == "First" &&
                            sortedCombatants[0].initiative.value == 11 &&
                            sortedCombatants[1].entity.name == "Second" &&
                            sortedCombatants[1].initiative.value == 10
                })
            }
        }
    }

    @Nested
    inner class AdvanceTurnTest {

        lateinit var actor1: TurnActor;
        lateinit var actor2: TurnActor;
        lateinit var trackerEntries: List<TrackerEntry>
        lateinit var expectedRolls: Array<DieRoll>

        val FIRST_NAME = "First"
        val SECOND_NAME = "Second"

        val MATCH_FIRST_ENTITY: (Combatant) -> Boolean = { it.entity.name == FIRST_NAME }
        val MATCH_SECOND_ENTITY: (Combatant) -> Boolean = { it.entity.name == SECOND_NAME }

        @BeforeEach
        fun beforeEach() {
            actor1 = mockk<TurnActor>(name = "Actor1 Mock")
            actor2 = mockk<TurnActor>(name = "Actor2 Mock")

            trackerEntries = listOf(
                aTrackerEntity(name = FIRST_NAME, dexMod = 2, actor = actor1, faction = PLAYER_FACTION),
                aTrackerEntity(name = SECOND_NAME, dexMod = 1, actor = actor2, faction = MONSTER_FACTION),
            )

            expectedRolls = arrayOf(
                D20 rolls 10,  // player1: 10 + 2 = 12
                D20 rolls 10   // player2: 12 + 1 = 13
            )
        }

        @Test
        fun `advanceTurn should let the first actor in initiative order take their turn`() {
            withFixedDice(*expectedRolls) {
                expectTurnForActor(actor1, object : MovementCombatCommand() {
                    override fun doPerform(combatScenario: CombatScenario) {}
                })

                val tracker = createCombatTracker(trackerEntries)

                tracker.advanceTurn()

                verify(exactly = 1) {
                    actor1.handleTurn(
                        match(MATCH_FIRST_ENTITY),
                        match { it.movementAvailable && it.round == 1},
                        any()
                    )
                }
                verify(exactly = 1) {
                    actor1.handleTurn(
                        match(MATCH_FIRST_ENTITY),
                        match { !it.movementAvailable && it.round == 1},
                        any()
                    )
                }
                verify(exactly = 0) {
                    actor2.handleTurn(
                        match(MATCH_SECOND_ENTITY),
                        any(),
                        any()
                    )
                }
            }
        }

        @Test
        fun `advance to the 2nd turn should let the second actor in initiative order take their turn`() {
            withFixedDice(*expectedRolls) {
                expectTurnForActor(actor1)

                expectTurnForActor(actor2, object : MovementCombatCommand() {
                    override fun doPerform(combatScenario: CombatScenario) {}
                })

                val tracker = createCombatTracker(trackerEntries)

                repeat(2) {
                    tracker.advanceTurn()
                }

                verify(exactly = 1) {
                    actor1.handleTurn(
                        match(MATCH_FIRST_ENTITY),
                        match { it.round == 1},
                        any()
                    )
                }
                verify(exactly = 1) {
                    actor2.handleTurn(
                        match(MATCH_SECOND_ENTITY),
                        match { it.movementAvailable && it.round == 1},
                        any()
                    )
                }
                verify(exactly = 1) {
                    actor2.handleTurn(
                        match(MATCH_SECOND_ENTITY),
                        match { !it.movementAvailable},
                        any()
                    )
                }
            }
        }

        @Test
        fun `advance to the 3rd turn should start a new round and let the first actor take their turn`() {
            withFixedDice(*expectedRolls) {
                expectTurnForActor(actor1)
                expectTurnForActor(actor2)
                expectTurnForActor(actor1)

                val tracker = createCombatTracker(trackerEntries)

                repeat(3) {
                    tracker.advanceTurn()
                }
                verify(exactly = 1) {
                    actor1.handleTurn(
                        match(MATCH_FIRST_ENTITY),
                        match { it.movementAvailable && it.round == 2 },
                        any()
                    )
                }
            }
        }
    }

    private fun expectTurnForActor(actor2: TurnActor, vararg combatCmd: MovementCombatCommand) {
        every { actor2.handleTurn(any(), any(), any()) } returnsMany combatCmd.toList() + listOf(null)
    }
}