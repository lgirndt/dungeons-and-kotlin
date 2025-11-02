import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.example.*
import org.example.CharacterClass.Warlock
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CharacterTest {

    @MockK
    lateinit var diceRoller: DiceRoller

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
        val target = Character.create(stats = StatBlock.create())
        val opponent = Character.create(hitPoints = 20)

        every { diceRoller.rollDie(Die.D20) } returns 10
        assertThat(target.currentWeapon, equalTo(null))

        val outcome = target.attack(opponent, diceRoller)

        assertThat(opponent.hitPoints, equalTo(20))
        assertThat(outcome, equalTo(AttackOutcome.MISS))
    }

    @Test
    fun `an attacker who does not meet AC misses the attack`() {
        val target = Character.create(stats = StatBlock.create(str = 13u))
        target.equip(Weapon.LONGSWORD)
        val opponent = Character.create(
            hitPoints = 20,
            armour = { _ -> 18 })

        every { diceRoller.rollDie(Die.D20) } returns 10

        val outcome = target.attack(opponent, diceRoller)

        assertThat(outcome.hasBeenHit, equalTo(false))
        assertThat(outcome.hitRoll, equalTo(10 + 1 + 1)) // d20 + str mod + prof bonus
    }

    @Test
    fun `a character can equip a weapon`() {
        val character = Character.create()
        assertThat(character.currentWeapon, equalTo(null))
        character.equip(Weapon.LONGSWORD)
        assertThat(character.currentWeapon, equalTo(Weapon.LONGSWORD))
    }

}