import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.mockk.junit5.MockKExtension
import org.example.*
import org.example.Die.Companion.D8
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

fun aCharacterWithWeapon(
    strMod: Int = 10,
    damageDie: Die = D8,
    damageType: DamageType = DamageType.Slashing
): PlayerCharacter {
    val char = PlayerCharacter.aPlayerCharacter(
        stats = StatBlock.fromModifiers(strMod = strMod),
        weapon = SOME_WEAPON.copy(
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
    fun `a character with custom stats and class`() {
        val myCharacter = PlayerCharacter.aPlayerCharacter(
            stats = SOME_STAT_BOCK.copyByInts(dex = 12, con = 14),
        )

        assertThat(myCharacter.stats.dex, equalTo(Stat(12)))
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
        assertThat(PlayerCharacter.aPlayerCharacter(level = level).proficiencyBonus.toInt(),
            equalTo(expectedBonus))
    }


    @Test
    fun `CharacterClasses have the proper name`() {
        assertThat(Barbarian().name, equalTo("Barbarian"))
    }

    @Test
    fun `a character receives normal damage`() {
        val character = PlayerCharacter.aPlayerCharacter(
            hitPoints = 20
        )
        val damageReceived = character.receiveDamage(8, DamageType.Force)
        assertThat(damageReceived, equalTo(8))
        assertThat(character.hitPoints, equalTo(20 - 8))
    }

    @Test
    fun `a character receives half damage from resistance`() {
        val character = PlayerCharacter.aPlayerCharacter(
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
        val character = PlayerCharacter.aPlayerCharacter(
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
        val character = PlayerCharacter.aPlayerCharacter(
            hitPoints = 20,
            damageModifiers = DamageModifiers(
                immunities = setOf(DamageType.Force)
            )
        )
        val damageReceived = character.receiveDamage(15, DamageType.Force)
        assertThat(damageReceived, equalTo(0))
        assertThat(character.hitPoints, equalTo(20)) // no damage
    }

// TODO: PlayerCharacter doesn't have castSpellAttack method yet. This is only in Character.
//    @Test
//    fun `a fighter cannot cast a spell attack`() {
//        val fighter = PlayerCharacter.aPlayerCharacter(
//            classFeatures = Fighter(),
//            stats = StatBlock.fromModifiers(chaMod = 3) // Charisma 16
//        )
//        val opponent = PlayerCharacter.aPlayerCharacter(
//            hitPoints = 30,
//            armourClass = 12
//        )
//
//        val spell = SOME_SPELL.copy()
//        val outcome = fighter.castSpellAttack(spell, SpellLevel.Cantrip, opponent, RollModifier.NORMAL)
//        assertThat(outcome, equalTo(null))
//    }

// TODO: PlayerCharacter doesn't have castSpellAttack method yet. This is only in Character.
//    @Test
//    fun `a warlock can cast a spell attack`() {
//        val warlock = PlayerCharacter.aPlayerCharacter(
//            classFeatures = Warlock(),
//            stats = StatBlock.fromModifiers(chaMod = 3) // Charisma 16
//        )
//        val opponent = PlayerCharacter.aPlayerCharacter(
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
//            assertThat(outcome.hasBeenHit, equalTo(true))
//            assertThat(outcome.damageDealt, equalTo(10))
//        }
//    }

}