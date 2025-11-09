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
) {
    if (onLevel == SpellLevel.Cantrip && spell.level != SpellLevel.Cantrip) {
        return
    }
    if (spell.level < onLevel) {
        return
    }
    val spellAsWeapon = spellAsWeapon(caster, spell)
    attack(object : Attacker {
        override val currentWeapon: Weapon? = spellAsWeapon
        override val position: Coordinate = caster.position
        override val stats: StatBlock = caster.stats
        override fun applyAttackModifiers(weapon: Weapon): Int =
            caster.spellCastingAbility.modifier + caster.proficiencyBonus.toInt()

        override fun isCriticalHit(hitRoll: DieRoll): Boolean = hitRoll.value == 20

    }, opponent, rollModifier)

}

private fun spellAsWeapon(caster: Caster, spell: AttackSpell): Weapon {
    return Weapon(
        name = spell.name,
        category = WeaponCategory.Simple, // TODO this does not make sense
        attackType = AttackType.Ranged,
        damageType = spell.damageType, // TODO get from spell
        modifierStat = { caster.spellCastingAbility },
        damageRoll = spell.damageRoll,
    )

}


class AttackSpell(
    val name: String,
    val school: SpellSchool,
    val level: SpellLevel,
    val damageType: DamageType,
    val damageRoll: DamageRoll
)



