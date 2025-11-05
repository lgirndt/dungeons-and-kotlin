import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.example.*
import org.example.CharacterClass.Barbarian
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
            damageType = damageType
        )
    )
    return char
}

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
            stats = SOME_STAT_BOCK.copyByInts(dex = 12, con = 14),
        )
        assertAll(
            { assertThat(myCharacter.stats.dex, equalTo(Stat(12))) },
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

        private inline fun hitting(
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
                armour = { 10 },
                damageModifiers = SOME_DAMAGE_MODIFIERS.copy(
                    vulnerabilities = if (opponentVulnerableTo != null) setOf(opponentVulnerableTo) else emptySet()
                )
            )

            val diceRolls =  listOf(D20 rolls hitRoll) + damageRolls.map { damageDie rolls it }

            withFixedDice(*diceRolls.toTypedArray()) {
                val outcome = attacker.attack(opponent)
                runTest(outcome, opponent)
            }

        }

        @Test
        fun `a hit does normal damage`() {
            hitting(attackerStrMod = 1, damageRolls = listOf(5)) { outcome, _ ->
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
                damageRolls = listOf(6)
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

        @Test
        fun `attacking with NORMAL modifier uses single die roll`() {
            val damageDie = D8
            val attacker = aCharacterWithWeapon(strMod = 2, damageDie = damageDie)
            val opponent = SOME_CHARACTER.copy(armour = { _ -> 10 })

            withFixedDice(
                D20 rolls 10,
                damageDie rolls 6
            ) {
                val outcome = attacker.attack(opponent, RollModifier.NORMAL)

                assertThat(outcome.hasBeenHit, equalTo(true))
                assertThat(
                    "Hit roll uses single d20 roll",
                    outcome.hitRoll, equalTo(10 + 2 + 1) // d20 + str mod + prof bonus
                )
                assertThat(outcome.damageDealt, equalTo(6 + 2)) // damage roll + str mod
            }
        }

        @Test
        fun `attacking with ADVANTAGE uses higher of two rolls to hit`() {
            val damageDie = D8
            val attacker = aCharacterWithWeapon(strMod = 2, damageDie = damageDie)
            val opponent = SOME_CHARACTER.copy(armour = { _ -> 10 })

            withFixedDice(
                D20 rolls 8,
                D20 rolls 15,  // higher roll should be used
                damageDie rolls 6
            ) {
                val outcome = attacker.attack(opponent, RollModifier.ADVANTAGE)

                assertThat(outcome.hasBeenHit, equalTo(true))
                assertThat(
                    "Hit roll uses higher of two d20 rolls",
                    outcome.hitRoll, equalTo(15 + 2 + 1) // max(8, 15) + str mod + prof bonus
                )
                assertThat(outcome.damageDealt, equalTo(6 + 2))
            }
        }

        @Test
        fun `attacking with DISADVANTAGE uses lower of two rolls to hit`() {
            val damageDie = D8
            val attacker = aCharacterWithWeapon(strMod = 2, damageDie = damageDie)
            val opponent = SOME_CHARACTER.copy(armour = { _ -> 10 })

            withFixedDice(
                D20 rolls 15,
                D20 rolls 8,  // lower roll should be used
                damageDie rolls 6
            ) {
                val outcome = attacker.attack(opponent, RollModifier.DISADVANTAGE)

                assertThat(outcome.hasBeenHit, equalTo(true))
                assertThat(
                    "Hit roll uses lower of two d20 rolls",
                    outcome.hitRoll, equalTo(8 + 2 + 1) // min(15, 8) + str mod + prof bonus
                )
                assertThat(outcome.damageDealt, equalTo(6 + 2))
            }
        }

        @Test
        fun `attacking with ADVANTAGE can turn a miss into a hit`() {
            val attacker = aCharacterWithWeapon(strMod = 1)
            val opponent = SOME_CHARACTER.copy(armour = { _ -> 15 })

            withFixedDice(
                D20 rolls 5,  // would miss: 5 + 1 + 1 = 7 < 15
                D20 rolls 13, // hits: 13 + 1 + 1 = 15
                D8 rolls 4
            ) {
                val outcome = attacker.attack(opponent, RollModifier.ADVANTAGE)

                assertThat(outcome.hasBeenHit, equalTo(true))
                assertThat(outcome.hitRoll, equalTo(13 + 1 + 1))
            }
        }

        @Test
        fun `attacking with DISADVANTAGE can turn a hit into a miss`() {
            val attacker = aCharacterWithWeapon(strMod = 1)
            val opponent = SOME_CHARACTER.copy(armour = { _ -> 15 })

            withFixedDice(
                D20 rolls 13, // would hit: 13 + 1 + 1 = 15
                D20 rolls 5   // misses: 5 + 1 + 1 = 7 < 15
            ) {
                val outcome = attacker.attack(opponent, RollModifier.DISADVANTAGE)

                assertThat(outcome.hasBeenHit, equalTo(false))
                assertThat(outcome.hitRoll, equalTo(5 + 1 + 1))
            }
        }

        @Test
        fun `critical hit works with ADVANTAGE when either roll is 20`() {
            val damageDie = D8
            val attacker = aCharacterWithWeapon(strMod = 2, damageDie = damageDie)
            val opponent = SOME_CHARACTER.copy(armour = { _ -> 10 })

            withFixedDice(
                D20 rolls 12,
                D20 rolls 20,  // critical hit
                damageDie rolls 5,
                damageDie rolls 7   // second damage roll for crit
            ) {
                val outcome = attacker.attack(opponent, RollModifier.ADVANTAGE)

                assertThat(outcome.hasBeenHit, equalTo(true))
                assertThat(outcome.damageDealt, equalTo(5 + 7 + 2)) // double dice + str mod
            }
        }

        @Test
        fun `critical hit works with DISADVANTAGE when higher roll is 20`() {
            val damageDie = D8
            val attacker = aCharacterWithWeapon(strMod = 2, damageDie = damageDie)
            val opponent = SOME_CHARACTER.copy(armour = { _ -> 10 })

            withFixedDice(
                D20 rolls 20,  // this is the max, so it's used even with disadvantage
                D20 rolls 20,  // both are 20, so critical hit triggers
                damageDie rolls 5,
                damageDie rolls 7   // second damage roll for crit
            ) {
                val outcome = attacker.attack(opponent, RollModifier.DISADVANTAGE)

                assertThat(outcome.hasBeenHit, equalTo(true))
                assertThat(outcome.damageDealt, equalTo(5 + 7 + 2)) // double dice + str mod
            }
        }

    }

    @Test
    fun `CharacterClasses have the proper name`() {
        assertThat(Barbarian.name, equalTo("Barbarian"))
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
        every { armour(any()) } returnsMany  listOf(15, 18)
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