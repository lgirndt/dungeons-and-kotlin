import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.example.*
import org.example.Die.Companion.D10
import org.example.Die.Companion.D20
import org.example.Die.Companion.D8
import org.example.WeaponCategory.Martial
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

fun aCharacterWithWeapon(
    strMod: Int = 10,
    damageDie: Die = D8,
    damageType: DamageType = DamageType.Slashing
): Character {
    val char = SOME_CHARACTER.copy(
        stats = StatBlock.fromModifiers(strMod = strMod)
    )
    char.equip(
        SOME_WEAPON.copy(
            damageRoll = SimpleDamageRoll(1, damageDie),
            damageType = damageType,
            rangeChecker = RangeCheckers.melee(5.0)
        )
    )
    return char
}

@ExtendWith(MockKExtension::class)
class CharacterTest {

    @Test
    fun `creating a Warlock should have the proper characterClass`() {
        val warlock = SOME_CHARACTER.copy(characterClass = Warlock())
        assertInstanceOf<Warlock>(warlock.characterClass)
    }

    @Test
    fun `a character with custom stats and class`() {
        val myCharacter = SOME_CHARACTER.copy(
            characterClass = Warlock(),
            stats = SOME_STAT_BOCK.copyByInts(dex = 12, con = 14),
        )

        assertThat(myCharacter.stats.dex, equalTo(Stat(12)))
        assertInstanceOf<Warlock>(myCharacter.characterClass)
    }

    @ParameterizedTest
    @CsvSource(
        "1, 1",
        "2, 1",
        "4, 1",
        "5, 2",
        "8, 2",
        "9, 3"
    )
    fun `proficiency bonus at various levels should be correct`(level: Int, expectedBonus: Int) {
        assertThat(SOME_CHARACTER.copy(level = level).proficiencyBonus, equalTo(expectedBonus))
    }


    @Test
    fun `CharacterClasses have the proper name`() {
        assertThat(Barbarian().name, equalTo("Barbarian"))
    }


    @Test
    fun `a character can equip a weapon`() {
        val character = SOME_CHARACTER.copy()
        assertThat(character.currentWeapon, equalTo(null))
        character.equip(Weapons.LONGSWORD)
        assertThat(character.currentWeapon, equalTo(Weapons.LONGSWORD))
    }

    @Test
    fun `let's understand property oneline assignments`() {
        val armour = mockk<(StatBlock) -> Int>()
        every { armour(any()) } returnsMany listOf(15, 18)
        val character = SOME_CHARACTER.copy(armour = armour)
        assertThat(character.armourClass, equalTo(15))
        assertThat(character.armourClass, equalTo(18))
    }


    @Test
    fun `a character receives normal damage`() {
        val character = SOME_CHARACTER.copy(
            hitPoints = 20
        )
        val damageReceived = character.receiveDamage(8, DamageType.Force)
        assertThat(damageReceived, equalTo(8))
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
        val damageReceived = character.receiveDamage(9, DamageType.Force)
        assertThat(damageReceived, equalTo(4))
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
        val damageReceived = character.receiveDamage(6, DamageType.Force)
        assertThat(damageReceived, equalTo(12))
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
        val damageReceived = character.receiveDamage(15, DamageType.Force)
        assertThat(damageReceived, equalTo(0))
        assertThat(character.hitPoints, equalTo(20)) // no damage
    }

}