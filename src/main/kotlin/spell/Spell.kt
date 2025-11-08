package org.example.spell

enum class SpellSchool {
    Abjuration,
    Conjuration,
    Evocation,
    // TODO add the others
}

sealed class SpellLevel(val level: Int) : Comparable<SpellLevel> {

    override fun compareTo(other: SpellLevel): Int = compareBy(SpellLevel::level).compare(this, other)

    object Cantrip : SpellLevel(0)
    object Level1: SpellLevel(1)
}

interface Caster {

}

interface SpellAffectable {

}

fun cast(caster: Caster, spell: Spell, onLevel: SpellLevel) {
    if (onLevel == SpellLevel.Cantrip && spell.level != SpellLevel.Cantrip) {
        return
    }
    if(spell.level < onLevel) {
        return
    }
}

open class Spell(
    val name: String,
    val school: SpellSchool,
    val level: SpellLevel,
) {
}
open class AttackSpell(
    name: String,
    school: SpellSchool,
    level: SpellLevel,
) : Spell(
    name,
    school,
    level,
) {

}


class FireBolt : Spell(
    name = "Fire Bolt",
    school = SpellSchool.Evocation,
    level = SpellLevel.Cantrip,
)

