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
            { assertThat(Stat(8u).modifier, equalTo(-1)) },
            { assertThat(Stat(9u).modifier, equalTo(-1)) },

            { assertThat(Stat(10u).modifier, equalTo(0)) },
            { assertThat(Stat(11u).modifier, equalTo(0)) },

            { assertThat(Stat(12u).modifier, equalTo(1)) },
            { assertThat(Stat(13u).modifier, equalTo(1)) },
        )
    }

    @Test
    fun `overloaded ctor should assign values correctly`() {
        val statBlocks = StatBlock(10u, 11u, 12u, 13u, 14u, 15u)
        assertAll(
            { assertThat(statBlocks.str.value, equalTo(10u)) },
            { assertThat(statBlocks.dex.value, equalTo(11u)) },
            { assertThat(statBlocks.con.value, equalTo(12u)) },
            { assertThat(statBlocks.int.value, equalTo(13u)) },
            { assertThat(statBlocks.wis.value, equalTo(14u)) },
            { assertThat(statBlocks.cha.value, equalTo(15u)) },
        )
    }

    @Test
    fun `some StatBlock`() {
        val dexBlock = StatBlock.create(dex = 12u)
        assertAll(
            { assertThat(dexBlock.dex.value, equalTo(12u)) },
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
        val stats = StatBlock.create(str = 16u)

        every { diceRoller.rollDie(D8) } returns 6

        val damage = longsword.dealDamage( stats, diceRoller)

        assertThat(damage, equalTo(6 + 3)) // 3 is the modifier for str 16
        verify(exactly = 1) { diceRoller.rollDie(D8) }
    }
}

