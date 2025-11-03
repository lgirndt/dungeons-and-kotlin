import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.example.*
import org.example.CharacterClass.Warlock
import org.example.Die.Companion.D10
import org.example.Die.Companion.D20
import org.example.Die.Companion.D8
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class CharacterTest {

    @MockK
    lateinit var diceRoller: DiceRoller

    @Test
    fun `creating a Warlock should have the proper characterClass`() {
        val warlock = Character.create(characterClass = Warlock)
        assertThat(warlock.characterClass, equalTo(Warlock))
    }

    @Test
    fun `a character with custom stats and class`() {
        val myCharacter = Character.create(
            characterClass = Warlock,
            stats = StatBlock.create(dex = 12, con = 14),
        )
        assertAll(
            { assertThat(myCharacter.stats.dex.value, equalTo(12)) },
            { assertThat(myCharacter.characterClass, equalTo(Warlock)) },
        )
    }

    @Test
    fun `proficiency bonus at various levels should be correct`() {
        assertAll(
            { assertThat(Character.create(level = 1).proficiencyBonus, equalTo(1)) },
            { assertThat(Character.create(level = 2).proficiencyBonus, equalTo(1)) },
            { assertThat(Character.create(level = 4).proficiencyBonus, equalTo(1)) },
            { assertThat(Character.create(level = 5).proficiencyBonus, equalTo(2)) },
            { assertThat(Character.create(level = 8).proficiencyBonus, equalTo(2)) },
            { assertThat(Character.create(level = 9).proficiencyBonus, equalTo(3)) },
        )
    }

    @Nested
    inner class AttackTest {

        fun aCharacterWithWeapon(str: Int = 10, damageDie : Die = D8): Character {
            val char = Character.create(
                stats = StatBlock.create(str = str)
            )
            char.equip(Weapon.create(damageDie = damageDie))
            return char
        }

        @Test
        fun `an attacker without a weapon cannot not attack`() {
            val attacker = Character.create()
            val opponent = Character.create(hitPoints = 20)

            expectDiceRolls(diceRoller, D20 rolls 10)
            assertThat(attacker.currentWeapon, equalTo(null))

            val outcome = attacker.attack(opponent, diceRoller)

            assertAll(
                { assertThat(opponent.hitPoints, equalTo(20)) },
                { assertThat(outcome, equalTo(AttackOutcome.MISS)) }
            )
        }

        @Test
        fun `an attacker who does not meet AC misses the attack`() {
            val target = aCharacterWithWeapon(str = 13)
            val opponent = Character.create(armour = { _ -> 13 })

            expectDiceRolls(diceRoller, D20 rolls 10)

            val outcome = target.attack(opponent, diceRoller)

            assertThat(outcome.hasBeenHit, equalTo(false))
            assertThat(
                "Hit Roll misses as d20 + str mod + prof bonus does not match AC",
                outcome.hitRoll, equalTo(10 + 1 + 1)
            )
        }

        @Test
        fun `an attacker who meets AC hits the attack`() {
            val damageDie = D8
            val attacker = aCharacterWithWeapon(str = 13, damageDie = damageDie)
            val opponent = Character.create(armour = { _ -> 10 + 1 + 1 })

            expectDiceRolls(diceRoller,
                D20 rolls 10,
                damageDie rolls 5
            )

            val outcome = attacker.attack(opponent, diceRoller)

            assertThat(outcome.hasBeenHit, equalTo(true))
            assertThat(
                "Hit Roll hits as d20 + str mod + prof bonus  matches AC",
                outcome.hitRoll, equalTo(10 + 1 + 1)
            )
        }

        fun hitting(
            attackerStr: Int,
            hitRoll : Int = 10,
            damageRoll: Int,
            runTest: (outcome: AttackOutcome)->Unit
        ) {
           hitting(attackerStr, hitRoll, listOf(damageRoll), runTest)
        }

        fun hitting(
            attackerStr: Int,
            hitRoll : Int = 10,
            damageRolls: List<Int>,
            runTest: (outcome: AttackOutcome)->Unit
        ) {
            val damageDie  = D10
            val attacker = aCharacterWithWeapon(str = attackerStr, damageDie)
            val opponent = Character.create(armour = { _ -> 10 })

            val diceRolls = mutableListOf<DieRoll>()
            diceRolls.add(D20 rolls hitRoll)
            damageRolls.forEach { damageRoll ->
                diceRolls.add(damageDie rolls damageRoll)
            }

            expectDiceRolls(diceRoller, *diceRolls.toTypedArray())

            val outcome = attacker.attack(opponent, diceRoller)
            runTest(outcome)
        }

        @Test
        fun `a hit does normal damage`() {
            hitting(attackerStr = 13, damageRoll = 5) {
                outcome ->
                assertThat(outcome.damageDealt, equalTo(5 + 1))
            }
        }

        @Test
        fun `a critical hit does double damage dice`() {
            hitting(
                attackerStr = 13,
                hitRoll = 20,
                damageRolls = listOf(5, 8)) {
                outcome ->
                assertThat(outcome.damageDealt, equalTo(5 + 8 + 1))
            }
        }

    }

    @Test
    fun `a character can equip a weapon`() {
        val character = Character.create()
        assertThat(character.currentWeapon, equalTo(null))
        character.equip(Weapons.LONGSWORD)
        assertThat(character.currentWeapon, equalTo(Weapons.LONGSWORD))
    }

}