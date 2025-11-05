import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
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
        val statBlocks = SOME_STAT_BOCK.copy(10, 11, 12, 13, 14, 15)
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
        val dexBlock = SOME_STAT_BOCK.copy(dex = 12)
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
        val simpleDamageRoll = SimpleDamageRoll(2, D6, 7)

        withFixedDice(
            D6 rolls 5,
            D6 rolls 3,
        ) {
            val result = simpleDamageRoll.roll(false)
            assertThat(result, equalTo(5 + 3 + 7))
        }

    }

    @Test
    fun `a weapon deals proper damage`() {
        val longsword = Weapons.LONGSWORD
        val stats = SOME_STAT_BOCK.copy(str = 16)

        withFixedDice(D8 rolls 6) {
            val damage = longsword.dealDamage(stats, false)

            assertThat(damage, equalTo(6 + 3)) // 3 is the modifier for str 16
        }

    }

    @Test
    fun `a crit deals double damage`() {
        val longsword = Weapons.LONGSWORD
        val stats = SOME_STAT_BOCK.copy(str = 16)

        withFixedDice(
            D8 rolls 4,
            D8 rolls 7,
        ) {
            val damage = longsword.dealDamage(stats, true)

            assertThat(damage, equalTo(4 + 7 + 3)) // 3 is the modifier for str 16
        }

    }

    @Test
    fun `a character receives normal damage`() {
        val character = SOME_CHARACTER.copy(
            hitPoints = 20
        )

        character.receiveDamage(8, DamageType.Force)

        assertThat(character.hitPoints, equalTo(20 - 8))
    }

    @Test
    fun `a character receives half damage from resistance`() {
        val character = SOME_CHARACTER.copy(
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
        val character = SOME_CHARACTER.copy(
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
        val character = SOME_CHARACTER.copy(
            hitPoints = 20,
            damageModifiers = DamageModifiers(
                immunities = setOf(DamageType.Force)
            )
        )
        character.receiveDamage(15, DamageType.Force)
        assertThat(character.hitPoints, equalTo(20)) // no damage
    }
}

