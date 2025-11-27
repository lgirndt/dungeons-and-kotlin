package io.dungeons

import io.dungeons.spell.SpellCasting

interface ClassFeatures {
    val name: String
        get() = this::class.simpleName!!

    fun isProficientWith(weapon: Weapon): Boolean // = weaponProficiency(weapon.category, weapon)

    fun isCriticalHit(die: DieRoll): Boolean = normalCrit(die)

    fun toSpellCaster(stats: StatBlock, level: Int): SpellCasting? = null
}

// missing classes
// Ranger, Rogue, Monk, Sorcerer, Wizard

private const val CRITICAL_HIT_FOR_FIGHTER = 19

private val normalCrit: (DieRoll) -> Boolean = { it.value == NATURAL_TWENTY }

class Fighter : ClassFeatures {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.all(weapon)

    override fun isCriticalHit(die: DieRoll): Boolean = die.value >= CRITICAL_HIT_FOR_FIGHTER
}

class Cleric : ClassFeatures {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.simple(weapon)
}

class Druid : ClassFeatures {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.simple(weapon)
}

class Barbarian : ClassFeatures {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.all(weapon)
}

class Paladin : ClassFeatures {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.none(weapon)
}

class Warlock : ClassFeatures {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.none(weapon)

    override fun toSpellCaster(stats: StatBlock, level: Int): SpellCasting? = SpellCasting(
        ability = StatQueries.Cha,
    )
}

class Bard : ClassFeatures {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.simple(weapon)
}
