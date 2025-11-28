package domain.combat

import domain.fromModifiers
import domain.rolls
import io.dungeons.domain.Die.Companion.D20
import io.dungeons.domain.DieRoll
import io.dungeons.domain.combat.ActionCombatCommand
import io.dungeons.domain.combat.BonusActionCombatCommand
import io.dungeons.domain.combat.CombatScenario
import io.dungeons.domain.combat.CombatTracker
import io.dungeons.domain.combat.CombatTrackerListener
import io.dungeons.domain.combat.Combatant
import io.dungeons.domain.combat.CombatantsCollection
import io.dungeons.domain.combat.Faction
import io.dungeons.domain.combat.MovementCombatCommand
import io.dungeons.domain.combat.SimpleCombatScenario
import io.dungeons.domain.combat.TurnActor
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

val PLAYER_FACTION = Faction(name = "Players")
val MONSTER_FACTION = Faction(name = "Monsters")

class CombatTrackerTest {
    fun aCombatant(
        name: String = "some name",
        dexMod: Int = 0,
        faction: Faction = PLAYER_FACTION,
        actor: TurnActor = mockk<TurnActor>(relaxed = true),
    ): Combatant = Combatant(
        creature = domain.aPlayerCharacter(
            name = name,
            stats = io.dungeons.domain.StatBlock.fromModifiers(dexMod = dexMod),
        ),
        faction = faction,
        actor = actor,
    )

    @Test
    fun `combatants should be sorted by initiative in descending order`() {
        val trackerEntries = listOf(
            aCombatant(name = "Slow Character", dexMod = 0),
            aCombatant(name = "Medium Character", dexMod = 2),
            aCombatant(name = "Fast Character", dexMod = 4),
        )
        domain.withFixedDice(
            D20 rolls 10, // lowDexPlayer: 10 + 0 = 10
            D20 rolls 12, // midDexPlayer: 12 + 2 = 14
            D20 rolls 8, // highDexPlayer: 8 + 4 = 12
        ) {
            val tracker = createCombatTracker(trackerEntries)

            // Verify the combatants are sorted by initiative (descending)
            // Expected order: midDexPlayer (14), highDexPlayer (12), lowDexPlayer (10)
            assertEquals(3, tracker.combatantsOrderedByInitiative.size)
            assertEquals("Medium Character", tracker.combatantsOrderedByInitiative[0].creature.name)
            assertEquals(14, tracker.combatantsOrderedByInitiative[0].initiative.value)
            assertEquals("Fast Character", tracker.combatantsOrderedByInitiative[1].creature.name)
            assertEquals(12, tracker.combatantsOrderedByInitiative[1].initiative.value)
            assertEquals("Slow Character", tracker.combatantsOrderedByInitiative[2].creature.name)
            assertEquals(10, tracker.combatantsOrderedByInitiative[2].initiative.value)
        }
    }

    @Test
    fun `combatants with same initiative should maintain input order`() {
        domain.withFixedDice(
            D20 rolls 10, // player1: 10 + 2 = 12
            D20 rolls 10, // player2: 10 + 2 = 12
        ) {
            val trackerEntries = listOf(
                aCombatant(name = "First", dexMod = 2),
                aCombatant(name = "Second", dexMod = 2),
            )

            val tracker = createCombatTracker(trackerEntries)

            // Both have initiative 12, should maintain input order
            assertEquals("First", tracker.combatantsOrderedByInitiative[0].creature.name)
            assertEquals("Second", tracker.combatantsOrderedByInitiative[1].creature.name)
        }
    }

    private fun toCombatScenario(combatants: List<Combatant>): SimpleCombatScenario =
        SimpleCombatScenario(toCombatStore(combatants))

    private fun toCombatStore(combatants: List<Combatant>): CombatantsCollection = CombatantsCollection(combatants)

    private fun createCombatTracker(combatants: List<Combatant>): CombatTracker = CombatTracker(
        combatants = combatants,
        combatScenario = toCombatScenario(combatants),
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
            aCombatant(name = "First", dexMod = 1),
            aCombatant(name = "Second", dexMod = 2),
        )
        domain.withFixedDice(
            D20 rolls 10, // player1: 10 + 1 = 11
            D20 rolls 8, // player2: 8 + 2 = 10
        ) {
            CombatTracker(
                combatants = trackerEntries,
                combatScenario = toCombatScenario(trackerEntries),
                listener = listener,
            )

            // Verify listener was called with the sorted list
            verify(exactly = 1) {
                listener.rolledInitiative(
                    match { sortedCombatants ->
                        sortedCombatants.size == 2 &&
                            sortedCombatants[0].creature.name == "First" &&
                            sortedCombatants[0].initiative.value == 11 &&
                            sortedCombatants[1].creature.name == "Second" &&
                            sortedCombatants[1].initiative.value == 10
                    },
                )
            }
        }
    }

    @Nested
    inner class AdvanceTurnTest {
        lateinit var actor1: TurnActor
        lateinit var actor2: TurnActor

        // TODO rename properly
        lateinit var combatants: List<Combatant>
        lateinit var expectedRolls: Array<DieRoll>
        lateinit var firstCombatant: Combatant
        lateinit var secondCombatant: Combatant

        val firstName = "First"
        val secondName = "Second"

        val matchFirstCreature: (Combatant) -> Boolean = { it.creature.name == firstName }
        val matchSecondCreature: (Combatant) -> Boolean = { it.creature.name == secondName }

        @BeforeEach
        fun beforeEach() {
            actor1 = mockk<TurnActor>(name = "Actor1 Mock")
            actor2 = mockk<TurnActor>(name = "Actor2 Mock")

            firstCombatant = aCombatant(name = firstName, dexMod = 2, actor = actor1, faction = PLAYER_FACTION)
            secondCombatant = aCombatant(name = secondName, dexMod = 1, actor = actor2, faction = MONSTER_FACTION)

            combatants = listOf(firstCombatant, secondCombatant)

            expectedRolls = arrayOf(
                D20 rolls 10, // player1: 10 + 2 = 12
                D20 rolls 10, // player2: 12 + 1 = 13
            )
        }

        @Test
        fun `advanceTurn should let the first actor in initiative order take their turn`() {
            domain.withFixedDice(*expectedRolls) {
                expectTurnForActor(
                    actor1,
                    object : MovementCombatCommand() {
                        override fun doPerform(combatScenario: CombatScenario) {}
                    },
                )

                val tracker = createCombatTracker(combatants)

                tracker.advanceTurn()

                verify(exactly = 1) {
                    actor1.handleTurn(
                        match(matchFirstCreature),
                        match { it.movementAvailable && it.round == 1 },
                        any(),
                    )
                }
                verify(exactly = 1) {
                    actor1.handleTurn(
                        match(matchFirstCreature),
                        match { !it.movementAvailable && it.round == 1 },
                        any(),
                    )
                }
                verify(exactly = 0) {
                    actor2.handleTurn(
                        match(matchSecondCreature),
                        any(),
                        any(),
                    )
                }
            }
        }

        @Test
        fun `advance to the 2nd turn should let the second actor in initiative order take their turn`() {
            domain.withFixedDice(*expectedRolls) {
                expectTurnForActor(actor1)

                expectTurnForActor(
                    actor2,
                    object : MovementCombatCommand() {
                        override fun doPerform(combatScenario: CombatScenario) {}
                    },
                )

                val tracker = createCombatTracker(combatants)

                repeat(2) {
                    tracker.advanceTurn()
                }

                verify(exactly = 1) {
                    actor1.handleTurn(
                        match(matchFirstCreature),
                        match { it.round == 1 },
                        any(),
                    )
                }
                verify(exactly = 1) {
                    actor2.handleTurn(
                        match(matchSecondCreature),
                        match { it.movementAvailable && it.round == 1 },
                        any(),
                    )
                }
                verify(exactly = 1) {
                    actor2.handleTurn(
                        match(matchSecondCreature),
                        match { !it.movementAvailable },
                        any(),
                    )
                }
            }
        }

        @Test
        fun `advance to the 3rd turn should start a new round and let the first actor take their turn`() {
            domain.withFixedDice(*expectedRolls) {
                expectTurnForActor(actor1)
                expectTurnForActor(actor2)
                expectTurnForActor(actor1)

                val tracker = createCombatTracker(combatants)

                repeat(3) {
                    tracker.advanceTurn()
                }
                verify(exactly = 1) {
                    actor1.handleTurn(
                        match(matchFirstCreature),
                        match { it.movementAvailable && it.round == 2 },
                        any(),
                    )
                }
            }
        }

        @Test
        fun `advanceTurn should skip combatants with 0 hit points`() {
            domain.withFixedDice(*expectedRolls) {
                expectTurnForActor(actor1)
                expectTurnForActor(actor2)

                val tracker = createCombatTracker(combatants)

                // First round - both actors take their turns normally
                tracker.advanceTurn()
                tracker.advanceTurn()

                // Reduce actor1's hit points to 0
                firstCombatant.creature.hitPoints = 0

                tracker.advanceTurn()

                // Verify actor1 was NOT called again (still only called once from round 1)
                verify(exactly = 1) {
                    actor1.handleTurn(
                        match(matchFirstCreature),
                        match { it.round == 1 },
                        any(),
                    )
                }
                verify(exactly = 1) {
                    actor2.handleTurn(
                        match(matchSecondCreature),
                        match { it.round == 1 },
                        any(),
                    )
                }
                verify(exactly = 0) {
                    actor1.handleTurn(
                        match(matchFirstCreature),
                        match { it.round == 2 },
                        any(),
                    )
                }
            }
        }

        @Test
        fun `actor using all three command types should exhaust turn options`() {
            domain.withFixedDice(*expectedRolls) {
                val actionCommand = object : ActionCombatCommand() {
                    override fun doPerform(combatScenario: CombatScenario) {}
                }

                val bonusActionCommand = object : BonusActionCombatCommand() {
                    override fun doPerform(combatScenario: CombatScenario) {}
                }

                val movementCommand = object : MovementCombatCommand() {
                    override fun doPerform(combatScenario: CombatScenario) {}
                }

                // Actor1 will use action, bonus action, and movement (in that order)
                every { actor1.handleTurn(any(), any(), any()) } returnsMany listOf(
                    actionCommand,
                    bonusActionCommand,
                    movementCommand,
                )

                val tracker = createCombatTracker(combatants)

                tracker.advanceTurn()

                // Verify actor1 was called 3 times:
                // 1. with all options available
                // 2. with action used
                // 3. with action and bonus action used
                verify(exactly = 1) {
                    actor1.handleTurn(
                        match(matchFirstCreature),
                        match { it.actionAvailable && it.bonusActionAvailable && it.movementAvailable },
                        any(),
                    )
                }
                verify(exactly = 1) {
                    actor1.handleTurn(
                        match(matchFirstCreature),
                        match { !it.actionAvailable && it.bonusActionAvailable && it.movementAvailable },
                        any(),
                    )
                }
                verify(exactly = 1) {
                    actor1.handleTurn(
                        match(matchFirstCreature),
                        match { !it.actionAvailable && !it.bonusActionAvailable && it.movementAvailable },
                        any(),
                    )
                }
                // Verify actor1 was NOT called a 4th time (all options exhausted)
                verify(exactly = 0) {
                    actor1.handleTurn(
                        match(matchFirstCreature),
                        match { !it.actionAvailable && !it.bonusActionAvailable && !it.movementAvailable },
                        any(),
                    )
                }
            }
        }
    }

    private fun expectTurnForActor(actor2: TurnActor, vararg combatCmd: MovementCombatCommand) {
        every { actor2.handleTurn(any(), any(), any()) } returnsMany combatCmd.toList() + listOf(null)
    }
}
