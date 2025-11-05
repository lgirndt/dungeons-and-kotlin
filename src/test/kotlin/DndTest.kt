import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.example.*
import org.example.CharacterClass.Barbarian
import org.example.Die.Companion.D6
import org.junit.jupiter.api.Assertions.assertAll
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

}

