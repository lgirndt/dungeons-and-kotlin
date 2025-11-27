package spell

import ProvidesBoardPositionMock
import SOME_STAT_BOCK
import TestId
import aPlayerCharacter
import io.dungeons.Creature
import io.dungeons.DamageType
import io.dungeons.Die.Companion.D20
import io.dungeons.Die.Companion.D4
import io.dungeons.ProficiencyBonus
import io.dungeons.RollModifier
import io.dungeons.SimpleDamageRoll
import io.dungeons.Stat
import io.dungeons.StatBlock
import io.dungeons.board.BoardPosition
import io.dungeons.core.Id
import io.dungeons.spell.AttackSpell
import io.dungeons.spell.Caster
import io.dungeons.spell.SpellLevel
import io.dungeons.spell.SpellSchool
import io.dungeons.spell.castAttackSpell
import io.dungeons.world.Coordinate
import io.dungeons.world.Feet
import io.dungeons.world.Square
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import rolls
import withFixedDice

private val SOME_SPELL = AttackSpell(
    name = "Magic Missile",
    school = SpellSchool.Evocation,
    level = SpellLevel.Level1,
    damageType = DamageType.Force,
    damageRoll = SimpleDamageRoll(1, D4, 1),
    range = Feet(120.0),
)

private val ID = TestId<Creature>()

class SpellTest {
    @Test
    fun `a spell attack that meets AC hits the target`() {
        val caster = object : Caster {
            override val id: Id<Creature> get() = ID[0]
            override val spellCastingAbility: Stat = Stat(14) // modifier +2
            override val proficiencyBonus: ProficiencyBonus = ProficiencyBonus.fromLevel(1) // +1
            override val position: Coordinate = Coordinate.from(0, 0)
            override val stats: StatBlock = SOME_STAT_BOCK
        }

        val spell = SOME_SPELL.copy(
            level = SpellLevel.Cantrip,
            damageRoll = SimpleDamageRoll(1, D4),
        )

        val opponent = aPlayerCharacter(
            id = ID[1],
            armourClass = 12,
        )

        val providesBoardPosition = ProvidesBoardPositionMock(
            mapOf(
                ID[0] to BoardPosition(Square(0), Square(0)),
                ID[1] to BoardPosition(Square(0), Square(2)),
            ),
        )

        withFixedDice(
            D20 rolls 10, // hit roll
            D4 rolls 6, // damage roll
        ) {
            val outcome = castAttackSpell(
                caster,
                opponent,
                spell,
                SpellLevel.Cantrip,
                providesBoardPosition,
                RollModifier.NORMAL,
            )

            // Hit roll: 10 (d20) + 2 (spell ability mod) + 1 (prof bonus) = 13, which meets AC 12
            // Damage: 6 (d8) + 2 (spell ability mod) = 8
            assertEquals(true, outcome.hasBeenHit)
        }
    }
}
