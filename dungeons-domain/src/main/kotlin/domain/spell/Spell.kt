package io.dungeons.domain.spell

import io.dungeons.domain.AttackOutcome
import io.dungeons.domain.AttackSource
import io.dungeons.domain.Attackable
import io.dungeons.domain.Attacker
import io.dungeons.domain.Creature
import io.dungeons.domain.DamageRoll
import io.dungeons.domain.DamageType
import io.dungeons.domain.DieRoll
import io.dungeons.domain.NATURAL_TWENTY
import io.dungeons.domain.ProficiencyBonus
import io.dungeons.domain.RangeChecker
import io.dungeons.domain.RangeCheckers
import io.dungeons.domain.RollModifier
import io.dungeons.domain.Stat
import io.dungeons.domain.StatBlock
import io.dungeons.domain.StatQuery
import io.dungeons.domain.attack
import io.dungeons.domain.combat.ProvidesBoardPosition
import io.dungeons.domain.world.Coordinate
import io.dungeons.domain.world.Feet
import io.dungeons.port.Id

enum class SpellSchool {
    Abjuration,
    Conjuration,
    Evocation,
    // TODO add the others
}

sealed class SpellLevel(val level: Int) : Comparable<SpellLevel> {
    override fun compareTo(other: SpellLevel): Int = compareBy(SpellLevel::level).compare(this, other)

    override fun toString(): String = "${this::class.simpleName}(level=$level)"

    object Cantrip : SpellLevel(0)

    object Level1 : SpellLevel(1)
}

interface Caster {
    val id: Id<Creature>
    val spellCastingAbility: Stat
    val proficiencyBonus: ProficiencyBonus
    val position: Coordinate
    val stats: StatBlock
}

fun castAttackSpell(
    caster: Caster,
    opponent: Attackable,
    spell: AttackSpell,
    onLevel: SpellLevel,
    providesBoardPosition: ProvidesBoardPosition,
    rollModifier: RollModifier,
): AttackOutcome {
    require(onLevel == SpellLevel.Cantrip && spell.level == SpellLevel.Cantrip)
    require(spell.level >= onLevel)

    val attackSource = spell.asAttackSource(caster)
    return attack(
        object : Attacker {
            override val id: Id<Creature>
                get() = caster.id
            override val attackSource = attackSource
            override val stats = caster.stats

            override fun applyAttackModifiers() = caster.spellCastingAbility.modifier + caster.proficiencyBonus.toInt()

            override fun isCriticalHit(hitRoll: DieRoll): Boolean = hitRoll.value == NATURAL_TWENTY
        },
        opponent,
        providesBoardPosition,
        rollModifier,
    )
}

data class AttackSpell(
    val name: String,
    val school: SpellSchool,
    val level: SpellLevel,
    val damageType: DamageType,
    val damageRoll: DamageRoll,
    val range: Feet,
) {
    internal fun asAttackSource(caster: Caster): AttackSource {
        val spell = this
        return object : AttackSource() {
            override val name: String = spell.name
            override val damageType: DamageType = spell.damageType
            override val statQuery: StatQuery = { caster.spellCastingAbility }
            override val damageRoll: DamageRoll = spell.damageRoll
            override val rangeChecker: RangeChecker = RangeCheckers.ranged(spell.range, spell.range)
        }
    }
}

class SpellCasting(val ability: StatQuery)
