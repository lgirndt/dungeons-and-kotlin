import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.mockk.MockK
import io.mockk.every
import io.mockk.mockk
import org.example.*
import org.example.CharacterClass.Barbarian
import org.example.CharacterClass.Warlock
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test

const val DEFAULT_STAT_VALUE = 10u

fun StatBlock.Companion.create(
    str: UInt = DEFAULT_STAT_VALUE,
    dex: UInt = DEFAULT_STAT_VALUE,
    con: UInt = DEFAULT_STAT_VALUE,
    int: UInt = DEFAULT_STAT_VALUE,
    wis: UInt = DEFAULT_STAT_VALUE,
    cha: UInt = DEFAULT_STAT_VALUE,
): StatBlock {
    return StatBlock(str, dex, con, int, wis, cha)
}

fun Character.Companion.create(
    name: String = "My Name",
    characterClass: CharacterClass = CharacterClass.Fighter,
    stats: StatBlock = StatBlock.create(),
    level: Int = 1,
    damageModifiers: DamageModifiers = DamageModifiers.NONE,
    currentWeapon: Weapon? = null,
    hitPoints: Int = 10,
    armour: (StatBlock) -> Int = { 10 },
): Character {
    return Character(name, characterClass, stats, level, damageModifiers, currentWeapon, hitPoints, armour)
}

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
    fun `create a Warlock`() {
        val warlock = Character.create(characterClass = Warlock)
        assertThat(warlock.characterClass, equalTo(Warlock))
    }

    @Test
    fun `a character with custom stats and class`() {
        val myCharacter = Character.create(
            characterClass = Warlock,
            stats = StatBlock.create(dex = 12u, con = 14u),
        )
        assertAll(
            { assertThat(myCharacter.stats.dex.value, equalTo(12u)) },
            { assertThat(myCharacter.characterClass, equalTo(Warlock)) },
        )
    }

    @Test
    fun `proficiency bonus at various levels`() {
        assertThat(
            Character.create(level = 1).proficiencyBonus, equalTo(1),
        )
        assertThat(
            Character.create(level = 2).proficiencyBonus, equalTo(1),
        )
        assertThat(
            Character.create(level = 4).proficiencyBonus, equalTo(1),
        )
        assertThat(
            Character.create(level = 5).proficiencyBonus, equalTo(2),
        )
        assertThat(
            Character.create(level = 8).proficiencyBonus, equalTo(2),
        )
        assertThat(
            Character.create(level = 9).proficiencyBonus, equalTo(3),
        )
    }

    @Test
    fun `an attacker without a weapon cannot not attack`() {
        val target = Character.create(stats = StatBlock.create(str = 13u))
        val opponent = Character.create(
            hitPoints = 20,
            armour = { _ -> 10 })

        val diceRoller = mockk<DiceRoller>()
        every { diceRoller.rollDie(Die.D20) } returns 10
        assertThat(target.currentWeapon, equalTo(null))

        val outcome = target.attack(opponent, diceRoller)

        assertThat(opponent.hitPoints, equalTo(20))
        assertThat(outcome, equalTo(AttackOutcome.MISS))
    }

    @Test
    fun `character can equip a weapon`() {
        val character = Character.create()
        assertThat(character.currentWeapon, equalTo(null))
        character.equip(Weapon.LONGSWORD)
        assertThat(character.currentWeapon, equalTo(Weapon.LONGSWORD))
    }
}

