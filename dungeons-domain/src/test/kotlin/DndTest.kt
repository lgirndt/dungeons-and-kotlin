
import org.junit.jupiter.api.Assertions.assertEquals
import io.mockk.mockk
import io.mockk.verify
import io.dungeons.AbilityCheckResult
import io.dungeons.Die.Companion.D20
import io.dungeons.Die.Companion.D6
import io.dungeons.RollModifier
import io.dungeons.SimpleDamageRoll
import io.dungeons.Stat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


class DndTest {


    @Test
    fun `a modifier should return the correct value`() {
        assertAll(
            { assertEquals(-1, Stat(8).modifier) },
            { assertEquals(-1, Stat(9).modifier) },

            { assertEquals(0, Stat(10).modifier) },
            { assertEquals(0, Stat(11).modifier) },

            { assertEquals(1, Stat(12).modifier) },
            { assertEquals(1, Stat(13).modifier) },
        )
    }

    @Test
    fun `overloaded ctor should assign values correctly`() {
        val statBlocks = SOME_STAT_BOCK.copyByInts(10, 11, 12, 13, 14, 15)
        assertAll(
            { assertEquals(Stat(10), statBlocks.str) },
            { assertEquals(Stat(11), statBlocks.dex) },
            { assertEquals(Stat(12), statBlocks.con) },
            { assertEquals(Stat(13), statBlocks.int) },
            { assertEquals(Stat(14), statBlocks.wis) },
            { assertEquals(Stat(15), statBlocks.cha) },
        )
    }

    @Test
    fun `some StatBlock`() {
        val dexBlock = SOME_STAT_BOCK.copyByInts(dex = 12)
        assertAll(
            { assertEquals(Stat(12), dexBlock.dex) },
            { assertEquals(DEFAULT_STAT_VALUE, dexBlock.str.toInt()) },
        )
    }

    @Test
    fun `SimpleDamageRoll for 1d6 should roll correctly`() {
        val simpleDamageRoll = SimpleDamageRoll(2, D6, 7)

        withFixedDice(
            D6 rolls 5,
            D6 rolls 3,
        ) {
            val result = simpleDamageRoll.roll(false)
            assertEquals(5 + 3 + 7, result)
        }

    }

    @Nested
    inner class RollModifierTest {

        @Test
        fun `NORMAL returns a single die roll`() {
            withFixedDice(D20 rolls 15) {
                val result = RollModifier.NORMAL.roll(D20)
                assertEquals(15, result.value)
            }
        }

        @Test
        fun `ADVANTAGE returns the higher of two rolls`() {
            withFixedDice(
                D20 rolls 8,
                D20 rolls 17
            ) {
                val result = RollModifier.ADVANTAGE.roll(D20)
                assertEquals(17, result.value)
            }
        }

        @Test
        fun `ADVANTAGE when first roll is higher`() {
            withFixedDice(
                D20 rolls 19,
                D20 rolls 12
            ) {
                val result = RollModifier.ADVANTAGE.roll(D20)
                assertEquals(19, result.value)
            }
        }

        @Test
        fun `ADVANTAGE when both rolls are equal`() {
            withFixedDice(
                D20 rolls 10,
                D20 rolls 10
            ) {
                val result = RollModifier.ADVANTAGE.roll(D20)
                assertEquals(10, result.value)
            }
        }

        @Test
        fun `DISADVANTAGE returns the lower of two rolls`() {
            withFixedDice(
                D20 rolls 8,
                D20 rolls 17
            ) {
                val result = RollModifier.DISADVANTAGE.roll(D20)
                assertEquals(8, result.value)
            }
        }

        @Test
        fun `DISADVANTAGE when second roll is lower`() {
            withFixedDice(
                D20 rolls 15,
                D20 rolls 3
            ) {
                val result = RollModifier.DISADVANTAGE.roll(D20)
                assertEquals(3, result.value)
            }
        }

        @Test
        fun `DISADVANTAGE when both rolls are equal`() {
            withFixedDice(
                D20 rolls 14,
                D20 rolls 14
            ) {
                val result = RollModifier.DISADVANTAGE.roll(D20)
                assertEquals(14, result.value)
            }
        }

        @Test
        fun `giveAdvantage works correctly`() {
            assertEquals(RollModifier.ADVANTAGE, RollModifier.NORMAL.giveAdvantage())
            assertEquals(RollModifier.NORMAL, RollModifier.DISADVANTAGE.giveAdvantage())
            assertEquals(RollModifier.ADVANTAGE, RollModifier.ADVANTAGE.giveAdvantage())
        }

        @Test
        fun `giveDisadvantage works correctly`() {
            assertEquals(RollModifier.DISADVANTAGE, RollModifier.NORMAL.giveDisadvantage())
            assertEquals(RollModifier.NORMAL, RollModifier.ADVANTAGE.giveDisadvantage())
            assertEquals(RollModifier.DISADVANTAGE, RollModifier.DISADVANTAGE.giveDisadvantage())
        }
    }

}

class AbilityCheckResultTest {

    @Test
    fun `a abilityCheckResult should have proper isSuccessful`() {
        assertEquals(true, AbilityCheckResult(true).isSuccessful)
        assertEquals(false, AbilityCheckResult(false).isSuccessful)
    }

    @Test
    fun `onSuccess should execute action when successful`() {
        val successAction = mockk<() -> Unit>(relaxed = true)
        val failureAction = mockk<() -> Unit>(relaxed = true)

        AbilityCheckResult(true)
            .onSuccess(successAction)
            .onFailure(failureAction)
        verify(exactly = 1) { successAction() }
        verify(exactly = 0) { failureAction() }
    }

    @Test
    fun `onFailure should execute action when not successful`() {
        val successAction = mockk<() -> Unit>(relaxed = true)
        val failureAction = mockk<() -> Unit>(relaxed = true)

        AbilityCheckResult(false)
            .onSuccess(successAction)
            .onFailure(failureAction)
        verify(exactly = 0) { successAction() }
        verify(exactly = 1) { failureAction() }
    }
}