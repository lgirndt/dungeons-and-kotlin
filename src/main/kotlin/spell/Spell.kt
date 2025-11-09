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

fun castRangeAttackSpell(
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
    val spellAsWeapon = spellAsWeapon(caster, spell)
    return attack(object : Attacker {
        override val currentWeapon: Weapon? = spellAsWeapon
        override val position: Coordinate = caster.position
        override val stats: StatBlock = caster.stats
        override fun applyAttackModifiers(weapon: Weapon): Int =
            caster.spellCastingAbility.modifier + caster.proficiencyBonus.toInt()

        override fun isCriticalHit(hitRoll: DieRoll): Boolean = hitRoll.value == 20

    }, opponent, rollModifier)

}

private fun spellAsWeapon(caster: Caster, spell: AttackSpell): Weapon {
    return spell.asWeapon(caster)
}


data class AttackSpell(
    val name: String,
    val school: SpellSchool,
    val level: SpellLevel,
    val damageType: DamageType,
    val damageRoll: DamageRoll,
    val range: Double) {

    fun asWeapon(caster: Caster): Weapon {
        val spell = this
        return object : Weapon() {
            override val name: String = spell.name
            override val category: WeaponCategory = WeaponCategory.Simple // TODO this does not make sense
            override val damageType: DamageType = spell.damageType
            override val statQuery: StatQuery = { caster.spellCastingAbility }
            override val damageRoll: DamageRoll = spell.damageRoll
            override fun isTargetInRange(distance: Double): RangeClassification =
                RangeCheckers.ranged(spell.range, spell.range).invoke(distance)
        }
    }
}



