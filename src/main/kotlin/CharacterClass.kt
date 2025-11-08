package org.example

interface CharacterClass {
    val name: String
        get() = this::class.simpleName!!

    fun isProficientWith(weapon: Weapon): Boolean // = weaponProficiency(weapon.category, weapon)
    fun isCriticalHit(die: DieRoll): Boolean = normalCrit(die)
}

// missing classes
// Ranger, Rogue, Monk, Sorcerer, Wizard

private val normalCrit : (DieRoll) -> Boolean = { it.value == 20 }

class Fighter : CharacterClass {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.all(weapon)
    override fun isCriticalHit(die: DieRoll): Boolean  = die.value >= 19
}

class Cleric() : CharacterClass {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.simple(weapon)
}

class Druid : CharacterClass {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.simple(weapon)
}

class Barbarian : CharacterClass {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.all(weapon)
}

class Paladin : CharacterClass {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.none(weapon)
}

class Warlock : CharacterClass {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.none(weapon)
}

class Bard : CharacterClass {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.simple(weapon)
}