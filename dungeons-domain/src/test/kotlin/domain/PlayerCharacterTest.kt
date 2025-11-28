package domain

import io.dungeons.domain.Barbarian
import io.dungeons.domain.DamageModifiers
import io.dungeons.domain.DamageType
import io.dungeons.domain.Die
import io.dungeons.domain.Die.Companion.D20
import io.dungeons.domain.Die.Companion.D8
import io.dungeons.domain.PlayerCharacter
import io.dungeons.domain.RangeCheckers
import io.dungeons.domain.RollModifier
import io.dungeons.domain.SimpleDamageRoll
import io.dungeons.domain.Stat
import io.dungeons.domain.StatBlock
import io.dungeons.domain.StatQueries
import io.dungeons.domain.world.Feet
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

fun aCharacterWithWeapon(
    strMod: Int = 10,
    damageDie: Die = D8,
    damageType: DamageType = DamageType.Slashing,
): PlayerCharacter {
    val char = aPlayerCharacter(
        stats = StatBlock.fromModifiers(strMod = strMod),
        weapon = SOME_WEAPON.copy(
            damageRoll = SimpleDamageRoll(1, damageDie),
            damageType = damageType,
            rangeChecker = RangeCheckers.melee(Feet(5.0)),
        ),
    )

    return char
}

@ExtendWith(MockKExtension::class)
class CharacterTest {
    @Test
    fun `a character with custom stats and class`() {
        val myCharacter = aPlayerCharacter(
            stats = SOME_STAT_BOCK.copyByInts(dex = 12, con = 14),
        )

        assertEquals(Stat(12), myCharacter.stats.dex)
    }

    @ParameterizedTest
    @CsvSource(
        "1, 1",
        "2, 1",
        "4, 1",
        "5, 2",
        "8, 2",
        "9, 3",
    )
    fun `proficiency bonus at various levels should be correct`(level: Int, expectedBonus: Int) {
        assertEquals(expectedBonus, aPlayerCharacter(level = level).proficiencyBonus.toInt())
    }

    @Test
    fun `CharacterClasses have the proper name`() {
        assertEquals("Barbarian", Barbarian().name)
    }

    @Test
    fun `a character receives normal damage`() {
        val character = aPlayerCharacter(
            hitPoints = 20,
        )
        val damageReceived = character.receiveDamage(8, DamageType.Force)
        assertEquals(8, damageReceived)
        assertEquals(20 - 8, character.hitPoints)
    }

    @Test
    fun `a character receives half damage from resistance`() {
        val character = aPlayerCharacter(
            hitPoints = 20,
            damageModifiers = DamageModifiers(
                resistances = setOf(DamageType.Force),
            ),
        )
        val damageReceived = character.receiveDamage(9, DamageType.Force)
        assertEquals(4, damageReceived)
        assertEquals(20 - 4, character.hitPoints) // half damage rounded down
    }

    @Test
    fun `a character receives double damage from vulnerability`() {
        val character = aPlayerCharacter(
            hitPoints = 20,
            damageModifiers = DamageModifiers(
                vulnerabilities = setOf(DamageType.Force),
            ),
        )
        val damageReceived = character.receiveDamage(6, DamageType.Force)
        assertEquals(12, damageReceived)
        assertEquals(20 - 6 * 2, character.hitPoints) // double damage
    }

    @Test
    fun `a character receives no damage from immunity`() {
        val character = aPlayerCharacter(
            hitPoints = 20,
            damageModifiers = DamageModifiers(
                immunities = setOf(DamageType.Force),
            ),
        )
        val damageReceived = character.receiveDamage(15, DamageType.Force)
        assertEquals(0, damageReceived)
        assertEquals(20, character.hitPoints) // no damage
    }

// TODO: PlayerCharacter doesn't have castSpellAttack method yet. This is only in Character.
//    @Test
//    fun `a fighter cannot cast a spell attack`() {
//        val fighter = aPlayerCharacter(
//            classFeatures = Fighter(),
//            stats = StatBlock.fromModifiers(chaMod = 3) // Charisma 16
//        )
//        val opponent = aPlayerCharacter(
//            hitPoints = 30,
//            armourClass = 12
//        )
//
//        val spell = SOME_SPELL.copy()
//        val outcome = fighter.castSpellAttack(spell, SpellLevel.Cantrip, opponent, RollModifier.NORMAL)
//        assertEquals(null, outcome)
//    }

// TODO: PlayerCharacter doesn't have castSpellAttack method yet. This is only in Character.
//    @Test
//    fun `a warlock can cast a spell attack`() {
//        val warlock = aPlayerCharacter(
//            classFeatures = Warlock(),
//            stats = StatBlock.fromModifiers(chaMod = 3) // Charisma 16
//        )
//        val opponent = aPlayerCharacter(
//            hitPoints = 30,
//            armourClass = 12
//        )
//
//        val spell = SOME_SPELL.copy(
//            level = SpellLevel.Cantrip,
//            damageRoll = SimpleDamageRoll(1, Die.D10),
//        )
//
//        withFixedDice(
//            Die.D20 rolls 12, // hit roll
//            Die.D10 rolls 7   // damage roll
//        ) {
//            val outcome = warlock.castSpellAttack(spell, SpellLevel.Cantrip, opponent, RollModifier.NORMAL)
//
//            assertNotNull(outcome)
//            // Hit roll: 12 (d20) + 3 (cha mod) + 2 (prof bonus) = 17, which meets AC 12
//            // Damage: 7 (d10) + 3 (cha mod) = 10
//            assertEquals(true, outcome.hasBeenHit)
//            assertEquals(10, outcome.damageDealt)
//        }
//    }

    @Test
    fun `ability check that meets DC should succeed`() {
        val character = aPlayerCharacter(
            stats = StatBlock.fromModifiers(strMod = 2), // Strength 14, modifier +2
        )

        withFixedDice(D20 rolls 10) {
            val result = character.rollAbilityCheck(
                ability = StatQueries.Str,
                difficultyClass = 12,
            )
            // Roll: 10 + 2 (str mod) = 12, meets DC 12
            assertEquals(true, result.isSuccessful)
        }
    }

    @Test
    fun `ability check that fails to meet DC should fail`() {
        val character = aPlayerCharacter(
            stats = StatBlock.fromModifiers(dexMod = 1), // Dexterity 12, modifier +1
        )

        withFixedDice(D20 rolls 8) {
            val result = character.rollAbilityCheck(
                ability = StatQueries.Dex,
                difficultyClass = 15,
            )
            // Roll: 8 + 1 (dex mod) = 9, fails DC 15
            assertEquals(false, result.isSuccessful)
        }
    }

    @Test
    fun `ability check with advantage should use higher roll`() {
        val character = aPlayerCharacter(
            stats = StatBlock.fromModifiers(wisMod = 3), // Wisdom 16, modifier +3
        )

        withFixedDice(
            D20 rolls 8,
            D20 rolls 15,
        ) {
            val result = character.rollAbilityCheck(
                ability = StatQueries.Wis,
                difficultyClass = 18,
                rollModifier = RollModifier.ADVANTAGE,
            )
            // Roll: 15 (higher) + 3 (wis mod) = 18, meets DC 18
            assertEquals(true, result.isSuccessful)
        }
    }

    @Test
    fun `ability check with disadvantage should use lower roll`() {
        val character = aPlayerCharacter(
            stats = StatBlock.fromModifiers(intMod = 2), // Intelligence 14, modifier +2
        )

        withFixedDice(
            D20 rolls 16,
            D20 rolls 9,
        ) {
            val result = character.rollAbilityCheck(
                ability = StatQueries.Int,
                difficultyClass = 15,
                rollModifier = RollModifier.DISADVANTAGE,
            )
            // Roll: 9 (lower) + 2 (int mod) = 11, fails DC 15
            assertEquals(false, result.isSuccessful)
        }
    }

    @Test
    fun `initiative roll should add dexterity modifier`() {
        val character = aPlayerCharacter(
            stats = StatBlock.fromModifiers(dexMod = 3), // Dexterity 16, modifier +3
        )

        withFixedDice(D20 rolls 12) {
            val result = character.rollInitiative()
            // Roll: 12 + 3 (dex mod) = 15
            assertEquals(15, result.value)
        }
    }

    @Test
    fun `initiative roll with advantage should use higher roll`() {
        val character = aPlayerCharacter(
            stats = StatBlock.fromModifiers(dexMod = 2), // Dexterity 14, modifier +2
        )

        withFixedDice(
            D20 rolls 8,
            D20 rolls 16,
        ) {
            val result = character.rollInitiative(RollModifier.ADVANTAGE)
            // Roll: 16 (higher) + 2 (dex mod) = 18
            assertEquals(18, result.value)
        }
    }

    @Test
    fun `initiative roll with disadvantage should use lower roll`() {
        val character = aPlayerCharacter(
            stats = StatBlock.fromModifiers(dexMod = 1), // Dexterity 12, modifier +1
        )

        withFixedDice(
            D20 rolls 14,
            D20 rolls 6,
        ) {
            val result = character.rollInitiative(RollModifier.DISADVANTAGE)
            // Roll: 6 (lower) + 1 (dex mod) = 7
            assertEquals(7, result.value)
        }
    }

    @Test
    fun `initiative roll with negative dexterity modifier should subtract`() {
        val character = aPlayerCharacter(
            stats = StatBlock.fromModifiers(dexMod = -1), // Dexterity 8, modifier -1
        )

        withFixedDice(D20 rolls 10) {
            val result = character.rollInitiative()
            // Roll: 10 + (-1) (dex mod) = 9
            assertEquals(9, result.value)
        }
    }

    @Test
    fun `initiative roll with zero dexterity modifier should not modify roll`() {
        val character = aPlayerCharacter(
            stats = StatBlock.fromModifiers(dexMod = 0), // Dexterity 10, modifier +0
        )

        withFixedDice(D20 rolls 15) {
            val result = character.rollInitiative()
            // Roll: 15 + 0 (dex mod) = 15
            assertEquals(15, result.value)
        }
    }

    @Test
    fun `initiative roll result should be a D20 die roll`() {
        val character = aPlayerCharacter(
            stats = StatBlock.fromModifiers(dexMod = 2),
        )

        withFixedDice(D20 rolls 10) {
            val result = character.rollInitiative()
            assertEquals(D20, result.die)
        }
    }
}
