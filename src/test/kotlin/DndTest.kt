
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.mockk.mockk
import io.mockk.verify
import org.example.AbilityCheckResult
import org.example.Die.Companion.D20
import org.example.Die.Companion.D6
import org.example.RollModifier
import org.example.SimpleDamageRoll
import org.example.Stat
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


class DndTest {


    @Test
    fun `a modifier should return the correct value`() {
        assertAll(
            { assertThat(Stat(8).modifier, equalTo(-1)) },
            { assertThat(Stat(9).modifier, equalTo(-1)) },

            { assertThat(Stat(10).modifier, equalTo(0)) },
            { assertThat(Stat(11).modifier, equalTo(0)) },

            { assertThat(Stat(12).modifier, equalTo(1)) },
            { assertThat(Stat(13).modifier, equalTo(1)) },
        )
    }

    @Test
    fun `overloaded ctor should assign values correctly`() {
        val statBlocks = SOME_STAT_BOCK.copyByInts(10, 11, 12, 13, 14, 15)
        assertAll(
            { assertThat(statBlocks.str, equalTo(Stat(10))) },
            { assertThat(statBlocks.dex, equalTo(Stat(11))) },
            { assertThat(statBlocks.con, equalTo(Stat(12))) },
            { assertThat(statBlocks.int, equalTo(Stat(13))) },
            { assertThat(statBlocks.wis, equalTo(Stat(14))) },
            { assertThat(statBlocks.cha, equalTo(Stat(15))) },
        )
    }

    @Test
    fun `some StatBlock`() {
        val dexBlock = SOME_STAT_BOCK.copyByInts(dex = 12)
        assertAll(
            { assertThat(dexBlock.dex, equalTo(Stat(12))) },
            { assertThat(dexBlock.str.toInt(), equalTo(DEFAULT_STAT_VALUE)) },
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
            assertThat(result, equalTo(5 + 3 + 7))
        }

    }

    @Nested
    inner class RollModifierTest {

        @Test
        fun `NORMAL returns a single die roll`() {
            withFixedDice(D20 rolls 15) {
                val result = RollModifier.NORMAL.roll(D20)
                assertThat(result.value, equalTo(15))
            }
        }

        @Test
        fun `ADVANTAGE returns the higher of two rolls`() {
            withFixedDice(
                D20 rolls 8,
                D20 rolls 17
            ) {
                val result = RollModifier.ADVANTAGE.roll(D20)
                assertThat(result.value, equalTo(17))
            }
        }

        @Test
        fun `ADVANTAGE when first roll is higher`() {
            withFixedDice(
                D20 rolls 19,
                D20 rolls 12
            ) {
                val result = RollModifier.ADVANTAGE.roll(D20)
                assertThat(result.value, equalTo(19))
            }
        }

        @Test
        fun `ADVANTAGE when both rolls are equal`() {
            withFixedDice(
                D20 rolls 10,
                D20 rolls 10
            ) {
                val result = RollModifier.ADVANTAGE.roll(D20)
                assertThat(result.value, equalTo(10))
            }
        }

        @Test
        fun `DISADVANTAGE returns the lower of two rolls`() {
            withFixedDice(
                D20 rolls 8,
                D20 rolls 17
            ) {
                val result = RollModifier.DISADVANTAGE.roll(D20)
                assertThat(result.value, equalTo(8))
            }
        }

        @Test
        fun `DISADVANTAGE when second roll is lower`() {
            withFixedDice(
                D20 rolls 15,
                D20 rolls 3
            ) {
                val result = RollModifier.DISADVANTAGE.roll(D20)
                assertThat(result.value, equalTo(3))
            }
        }

        @Test
        fun `DISADVANTAGE when both rolls are equal`() {
            withFixedDice(
                D20 rolls 14,
                D20 rolls 14
            ) {
                val result = RollModifier.DISADVANTAGE.roll(D20)
                assertThat(result.value, equalTo(14))
            }
        }

        @Test
        fun `giveAdvantage works correctly`() {
            assertThat(RollModifier.NORMAL.giveAdvantage(), equalTo(RollModifier.ADVANTAGE))
            assertThat(RollModifier.DISADVANTAGE.giveAdvantage(), equalTo(RollModifier.NORMAL))
            assertThat(RollModifier.ADVANTAGE.giveAdvantage(), equalTo(RollModifier.ADVANTAGE))
        }

        @Test
        fun `giveDisadvantage works correctly`() {
            assertThat(RollModifier.NORMAL.giveDisadvantage(), equalTo(RollModifier.DISADVANTAGE))
            assertThat(RollModifier.ADVANTAGE.giveDisadvantage(), equalTo(RollModifier.NORMAL))
            assertThat(RollModifier.DISADVANTAGE.giveDisadvantage(), equalTo(RollModifier.DISADVANTAGE))
        }
    }

}

class AbilityCheckResultTest {

    @Test
    fun `a abilityCheckResult should have proper isSuccessful`() {
        assertThat(AbilityCheckResult(true).isSuccessful, equalTo(true))
        assertThat(AbilityCheckResult(false).isSuccessful, equalTo(false))
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