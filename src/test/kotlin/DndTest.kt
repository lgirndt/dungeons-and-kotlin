import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.example.*
import org.example.CharacterClass.Barbarian
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

        val result = simpleDamageRoll.roll(diceRoller, false)
        assertThat(result, equalTo(5 + 3 + 7))
        verify(exactly = 2) { diceRoller.rollDie(D6) }
    }

    @Test
    fun `a weapon deals proper damage`() {
        val diceRoller = mockk<DiceRoller>()
        val longsword = Weapons.LONGSWORD
        val stats = StatBlock.create(str = 16)

        every { diceRoller.rollDie(D8) } returns 6

        val damage = longsword.dealDamage(stats, diceRoller, false)

        assertThat(damage, equalTo(6 + 3)) // 3 is the modifier for str 16
        verify(exactly = 1) { diceRoller.rollDie(D8) }
    }

    @Test
    fun `a crit deals double damage`() {
        val diceRoller = mockk<DiceRoller>()
        val longsword = Weapons.LONGSWORD
        val stats = StatBlock.create(str = 16)

        expectDiceRolls(diceRoller,
            D8 rolls 4,
            D8 rolls 7,
        )

        val damage = longsword.dealDamage(stats, diceRoller, true)

        assertThat(damage, equalTo(4 + 7 + 3)) // 3 is the modifier for str 16
        verify(exactly = 2) { diceRoller.rollDie(D8) }
    }

    @Test
    fun `a character receives normal damage`() {
        val character = Character.create(
            hitPoints = 20
        )

        character.receiveDamage(8, DamageType.Force)

        assertThat(character.hitPoints, equalTo(20 - 8))
    }

    @Test
    fun `a character receives half damage from resistance`() {
        val character = Character.create(
            hitPoints = 20,
            damageModifiers = DamageModifiers(
                resistances = setOf(DamageType.Force)
            )
        )
        character.receiveDamage(9, DamageType.Force)
        assertThat(character.hitPoints, equalTo(20 - 4)) // half damage rounded down
    }

    @Test
    fun `a character receives double damage from vulnerability`() {
        val character = Character.create(
            hitPoints = 20,
            damageModifiers = DamageModifiers(
                vulnerabilities = setOf(DamageType.Force)
            )
        )
        character.receiveDamage(6, DamageType.Force)
        assertThat(character.hitPoints, equalTo(20 - 6 * 2)) // double damage
    }

    @Test
    fun `a character receives no damage from immunity`() {
        val character = Character.create(
            hitPoints = 20,
            damageModifiers = DamageModifiers(
                immunities = setOf(DamageType.Force)
            )
        )
        character.receiveDamage(15, DamageType.Force)
        assertThat(character.hitPoints, equalTo(20)) // no damage
    }
}

