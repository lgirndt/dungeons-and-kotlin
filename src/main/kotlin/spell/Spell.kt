package org.example.spell

import org.example.*


enum class SpellSchool {
    Abjuration,
    Conjuration,
    Evocation,
    // TODO add the others
}

sealed class SpellLevel(val level: Int) : Comparable<SpellLevel> {

    override fun compareTo(other: SpellLevel): Int = compareBy(SpellLevel::level).compare(this, other)
    override fun toString(): String {
        return "SpellLevel(level=$level)"
    }

    object Cantrip : SpellLevel(0)
    object Level1 : SpellLevel(1)
}

interface Caster {
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
    rollModifier: RollModifier
) : AttackOutcome {
    if (onLevel == SpellLevel.Cantrip && spell.level != SpellLevel.Cantrip) {
        return AttackOutcome.MISS
    }
    if (spell.level < onLevel) {
        return AttackOutcome.MISS
    }
    val attackSource = spell.asAttackSource(caster)
    return attack(object : Attacker {
        override val attackSource = attackSource
        override val position = caster.position
        override val stats = caster.stats
        override fun applyAttackModifiers() =
            caster.spellCastingAbility.modifier + caster.proficiencyBonus.toInt()

        override fun isCriticalHit(hitRoll: DieRoll): Boolean = hitRoll.value == 20

    }, opponent, rollModifier)

}

data class AttackSpell(
    val name: String,
    val school: SpellSchool,
    val level: SpellLevel,
    val damageType: DamageType,
    val damageRoll: DamageRoll,
    val range: Double) {

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

class SpellCasting (
    val ability: StatQuery,
)

