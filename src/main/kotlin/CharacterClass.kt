package org.example

import org.example.spell.SpellCasting

interface CharacterClass {
    val name: String
        get() = this::class.simpleName!!

    fun isProficientWith(attackSource: AttackSource): Boolean // = weaponProficiency(weapon.category, weapon)
    fun isCriticalHit(die: DieRoll): Boolean = normalCrit(die)
    fun toSpellCaster(stats: StatBlock, level: Int) : SpellCasting? = null
}

// missing classes
// Ranger, Rogue, Monk, Sorcerer, Wizard

private val normalCrit : (DieRoll) -> Boolean = { it.value == 20 }

class Fighter : CharacterClass {
    override fun isProficientWith(attackSource: AttackSource): Boolean = WeaponProficiencies.all(attackSource)
    override fun isCriticalHit(die: DieRoll): Boolean  = die.value >= 19
}

class Cleric : CharacterClass {
    override fun isProficientWith(attackSource: AttackSource): Boolean = WeaponProficiencies.simple(attackSource)
}

class Druid : CharacterClass {
    override fun isProficientWith(attackSource: AttackSource): Boolean = WeaponProficiencies.simple(attackSource)
}

class Barbarian : CharacterClass {
    override fun isProficientWith(attackSource: AttackSource): Boolean = WeaponProficiencies.all(attackSource)
}

class Paladin : CharacterClass {
    override fun isProficientWith(attackSource: AttackSource): Boolean = WeaponProficiencies.none(attackSource)
}

class Warlock : CharacterClass {
    override fun isProficientWith(attackSource: AttackSource): Boolean = WeaponProficiencies.none(attackSource)
    override fun toSpellCaster(stats: StatBlock, level: Int): SpellCasting? {
        return SpellCasting(
            ability = StatQueries.Cha,
        )
    }
}

class Bard : CharacterClass {
    override fun isProficientWith(attackSource: AttackSource): Boolean = WeaponProficiencies.simple(attackSource)
}