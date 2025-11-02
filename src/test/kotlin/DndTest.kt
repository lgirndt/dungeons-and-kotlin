import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.example.*
import org.example.CharacterClass.Barbarian
import org.example.Die.Companion.D20
import org.example.Die.Companion.D6
import org.example.Die.Companion.D8
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
        val statBlocks = StatBlock(10, 11, 12, 13, 14, 15)
        assertAll(
            { assertThat(statBlocks.str.value, equalTo(10)) },
            { assertThat(statBlocks.dex.value, equalTo(11)) },
            { assertThat(statBlocks.con.value, equalTo(12)) },
            { assertThat(statBlocks.int.value, equalTo(13)) },
            { assertThat(statBlocks.wis.value, equalTo(14)) },
            { assertThat(statBlocks.cha.value, equalTo(15)) },
        )
    }

    @Test
    fun `some StatBlock`() {
        val dexBlock = StatBlock.create(dex = 12)
        assertAll(
            { assertThat(dexBlock.dex.value, equalTo(12)) },
            { assertThat(dexBlock.str.value, equalTo(DEFAULT_STAT_VALUE)) },
        )
    }

    @Test
    fun `CharacterClasses have the proper name`() {
        assertThat(Barbarian.name, equalTo("Barbarian"))
    }

    @Test
    fun `SimpleDamageRoll for 1d6 should roll correctly`() {
        val diceRoller = mockk<DiceRoller>()
        val simpleDamageRoll = SimpleDamageRoll(2, D6, 7)

        expectDiceRolls(diceRoller,
            D6 rolls 5,
            D6 rolls 3,
        )

        val result = simpleDamageRoll.roll(diceRoller)
        assertThat(result, equalTo(5 + 3 + 7))
        verify(exactly = 2) { diceRoller.rollDie(D6) }
    }

    @Test
    fun `a weapon deals proper damage`() {
        val diceRoller = mockk<DiceRoller>()
        val longsword = Weapon.LONGSWORD
        val stats = StatBlock.create(str = 16)

        every { diceRoller.rollDie(D8) } returns 6

        val damage = longsword.dealDamage( stats, diceRoller)

        assertThat(damage, equalTo(6 + 3)) // 3 is the modifier for str 16
        verify(exactly = 1) { diceRoller.rollDie(D8) }
    }
}

