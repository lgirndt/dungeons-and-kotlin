package org.example.spell

import org.example.Cleric

enum class SpellSchool {
    Abjuration,
    Conjuration,
    Evocation,
    // TODO add the others
}

sealed class SpellLevel(level: Int) {
    object Cantrip : SpellLevel(0)
    object Level1: SpellLevel(1)
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
    open fun cast(caster: Cleric, target: org.example.Attackable): org.example.AttackOutcome {
        // default implementation does nothing
        return org.example.AttackOutcome.MISS
    }
}


class FireBolt : Spell(
    name = "Fire Bolt",
    school = SpellSchool.Evocation,
    level = SpellLevel.Cantrip,
)

