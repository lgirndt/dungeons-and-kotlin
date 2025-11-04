import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@ExtendWith(MockKExtension::class)
class CharacterTest {

    @Test
    fun `creating a Warlock should have the proper characterClass`() {
        val warlock = SOME_CHARACTER.copy(characterClass = Warlock)
        assertThat(warlock.characterClass, equalTo(Warlock))
    }

    @Test
    fun `a character with custom stats and class`() {
        val myCharacter = SOME_CHARACTER.copy(
            characterClass = Warlock,
            stats = SOME_STAT_BOCK.copy(dex = 12, con = 14),
        )
        assertAll(
            { assertThat(myCharacter.stats.dex.value, equalTo(12)) },
            { assertThat(myCharacter.characterClass, equalTo(Warlock)) },
        )
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

    @Nested
    inner class AttackTest {

        fun aCharacterWithWeapon(
            strMod: Int = 10,
            damageDie: Die = D8,
            damageType: DamageType = DamageType.Slashing
        ): Character {
            val char = SOME_CHARACTER.copy(
                stats = StatBlock.createWithModifiers(strMod = strMod)
            )
            char.equip(
                SOME_WEAPON.copy(
                    damageRoll = SimpleDamageRoll(1, damageDie),
                    damageType = damageType
                )
            )
            return char
        }

        @Test
        fun `an attacker without a weapon cannot not attack`() {
            val attacker = SOME_CHARACTER.copy()
            val opponent = SOME_CHARACTER.copy(hitPoints = 20)

            assertThat(attacker.currentWeapon, equalTo(null))
            val outcome = attacker.attack(opponent)

            assertThat(opponent.hitPoints, equalTo(20))
            assertThat(outcome, equalTo(AttackOutcome.MISS))
        }

        @Test
        fun `an attacker who does not meet AC misses the attack`() {
            val target = aCharacterWithWeapon(strMod = 1)
            val opponent = SOME_CHARACTER.copy(armour = { _ -> 13 })

            withFixedDice(D20 rolls 10) {
                val outcome = target.attack(opponent)

                assertThat(outcome.hasBeenHit, equalTo(false))
                assertThat(
                    "Hit Roll misses as d20 + str mod + prof bonus does not match AC",
                    outcome.hitRoll, equalTo(10 + 1 + 1)
                )
            }

        }

        @Test
        fun `an attacker who meets AC hits the attack`() {
            val damageDie = D8
            val attacker = aCharacterWithWeapon(strMod = 1, damageDie = damageDie)
            val opponent = SOME_CHARACTER.copy(armour = { _ -> 10 + 1 + 1 })

            withFixedDice(
                D20 rolls 10,
                damageDie rolls 5
            ) {
                val outcome = attacker.attack(opponent)

                assertThat(outcome.hasBeenHit, equalTo(true))
                assertThat(
                    "Hit Roll hits as d20 + str mod + prof bonus  matches AC",
                    outcome.hitRoll, equalTo(10 + 1 + 1)
                )
            }

        }

        fun hitting(
            attackerStrMod: Int,
            opponentHitPoints: Int = 20,
            hitRoll: Int = 10,
            damageRoll: Int,
            damageType: DamageType = DamageType.Slashing,
            opponentVulnerableTo: DamageType? = null,
            runTest: (outcome: AttackOutcome, opponent: Character) -> Unit
        ) {
            hitting(
                attackerStrMod,
                opponentHitPoints,
                hitRoll,
                listOf(damageRoll),
                damageType,
                opponentVulnerableTo,
                runTest
            )
        }

        fun hitting(
            attackerStrMod: Int,
            opponentHitPoints: Int = 20,
            hitRoll: Int = 10,
            damageRolls: List<Int>,
            damageType: DamageType = DamageType.Slashing,
            opponentVulnerableTo: DamageType? = null,
            runTest: (outcome: AttackOutcome, opponent: Character) -> Unit
        ) {
            val damageDie = D10
            val attacker = aCharacterWithWeapon(
                strMod = attackerStrMod,
                damageType = damageType,
                damageDie = damageDie
            )
            val opponent = SOME_CHARACTER.copy(
                hitPoints = opponentHitPoints,
                armour = { _ -> 10 },
                damageModifiers = SOME_DAMAGE_MODIFIERS.copy(
                    vulnerabilities = if (opponentVulnerableTo != null) setOf(opponentVulnerableTo) else emptySet()
                )
            )

            val diceRolls = mutableListOf<DieRoll>()
            diceRolls.add(D20 rolls hitRoll)
            damageRolls.forEach { damageRoll ->
                diceRolls.add(damageDie rolls damageRoll)
            }

            withFixedDice(*diceRolls.toTypedArray()) {
                val outcome = attacker.attack(opponent)
                runTest(outcome, opponent)
            }

        }

        @Test
        fun `a hit does normal damage`() {
            hitting(attackerStrMod = 1, damageRoll = 5) { outcome, _ ->
                assertThat(outcome.damageDealt, equalTo(5 + 1))
            }
        }

        @Test
        fun `a critical hit does double damage dice`() {
            hitting(
                attackerStrMod = 1,
                hitRoll = 20,
                damageRolls = listOf(5, 8)
            ) { outcome, _ ->
                assertThat(outcome.damageDealt, equalTo(5 + 8 + 1))
            }
        }

        @Test
        fun `an opponent receives damage properly`() {
            hitting(
                attackerStrMod = 2,
                opponentHitPoints = 30,
                damageRoll = 6
            ) { _, opponent ->
                assertThat(opponent.hitPoints, equalTo(30 - 6 - 2))
            }
        }

        @Test
        fun `one hitting with a crit on vulnerability`() {
            hitting(
                attackerStrMod = 2,
                opponentHitPoints = 38,
                hitRoll = 20,
                damageRolls = listOf(8, 9),
                damageType = DamageType.Slashing,
                opponentVulnerableTo = DamageType.Slashing
            ) { _, opponent ->
                assertThat(opponent.hitPoints, equalTo(0))
            }
        }

    }

    @Test
    fun `a character can equip a weapon`() {
        val character = SOME_CHARACTER.copy()
        assertThat(character.currentWeapon, equalTo(null))
        character.equip(Weapons.LONGSWORD)
        assertThat(character.currentWeapon, equalTo(Weapons.LONGSWORD))
    }

}