package spell

import SOME_CHARACTER
import SOME_STAT_BOCK
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.example.*
import org.example.Die.Companion.D20
import org.example.Die.Companion.D4
import org.example.spell.*
import org.junit.jupiter.api.Test
import rolls
import withFixedDice

val SOME_SPELL = AttackSpell(
    name = "Magic Missile",
    school = SpellSchool.Evocation,
    level = SpellLevel.Level1,
    damageType = DamageType.Force,
    damageRoll = SimpleDamageRoll(1, Die.D4, 1),
    range = 120.0
)

class SpellTest {

    @Test
    fun `a spell attack that meets AC hits the target`() {
        val caster = object : Caster {
            override val spellCastingAbility: Stat = Stat(14) // modifier +2
            override val proficiencyBonus: ProficiencyBonus = ProficiencyBonus.fromLevel(1) // +1
            override val position: Coordinate = Coordinate(0, 0)
            override val stats: StatBlock = SOME_STAT_BOCK
        }

        val spell = SOME_SPELL.copy(
            level = SpellLevel.Cantrip,
            damageRoll = SimpleDamageRoll(1, D4)
        )

        val opponent = SOME_CHARACTER.copy(
            armour = { 12 }, // AC 12
            position = Coordinate(10, 0)
        )

        withFixedDice(
            D20 rolls 10, // hit roll
            D4 rolls 6    // damage roll
        ) {
            val outcome = castAttackSpell(caster, opponent, spell, SpellLevel.Cantrip, RollModifier.NORMAL)

            // Hit roll: 10 (d20) + 2 (spell ability mod) + 1 (prof bonus) = 13, which meets AC 12
            // Damage: 6 (d8) + 2 (spell ability mod) = 8
            assertThat(outcome.hasBeenHit, equalTo(true))
        }
    }
}