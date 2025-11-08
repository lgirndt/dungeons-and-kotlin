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
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.all(weapon.category, weapon)
    override fun isCriticalHit(die: DieRoll): Boolean  = die.value >= 19
}

class Cleric() : CharacterClass {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.simple(weapon.category, weapon)
}

class Druid : CharacterClass {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.simple(weapon.category, weapon)
}

class Barbarian : CharacterClass {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.all(weapon.category, weapon)
}

class Paladin : CharacterClass {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.none(weapon.category, weapon)
}

class Warlock : CharacterClass {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.none(weapon.category, weapon)
}

class Bard : CharacterClass {
    override fun isProficientWith(weapon: Weapon): Boolean = WeaponProficiencies.simple(weapon.category, weapon)
}