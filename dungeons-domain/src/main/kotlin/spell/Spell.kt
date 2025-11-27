package io.dungeons.spell

import com.google.common.base.MoreObjects
import io.dungeons.AttackOutcome
import io.dungeons.AttackSource
import io.dungeons.Attackable
import io.dungeons.Attacker
import io.dungeons.Creature
import io.dungeons.DamageRoll
import io.dungeons.DamageType
import io.dungeons.DieRoll
import io.dungeons.ProficiencyBonus
import io.dungeons.RangeChecker
import io.dungeons.RangeCheckers
import io.dungeons.RollModifier
import io.dungeons.Stat
import io.dungeons.StatBlock
import io.dungeons.StatQuery
import io.dungeons.attack
import io.dungeons.combat.ProvidesBoardPosition
import io.dungeons.core.Id
import io.dungeons.world.Coordinate
import io.dungeons.world.Feet

enum class SpellSchool {
    Abjuration,
    Conjuration,
    Evocation,
    // TODO add the others
}

sealed class SpellLevel(val level: Int) : Comparable<SpellLevel> {
    override fun compareTo(other: SpellLevel): Int = compareBy(SpellLevel::level).compare(this, other)

    override fun toString(): String = MoreObjects
        .toStringHelper(this)
        .add("level", level)
        .toString()

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

            override fun isCriticalHit(hitRoll: DieRoll): Boolean = hitRoll.value == 20
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
