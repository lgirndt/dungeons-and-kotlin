package org.example

open class CharacterClass(
    private val hitDie: Die,
    private val weaponProficiency: WeaponProficiency = WeaponProficiencies.none,
    val isCriticalHit: (DieRoll) -> Boolean = { roll -> roll.value == 20 }
) {
    val name: String
        get() = this::class.simpleName!!

    fun isProficientWith(weapon: Weapon): Boolean = weaponProficiency(weapon.category, weapon)

//    data object Cleric : CharacterClass(Die.D8, WeaponProficiencies.simple)
//    data object Druid : CharacterClass(Die.D8, WeaponProficiencies.simple)
//    data object Barbarian : CharacterClass(Die.D12, WeaponProficiencies.all)
//    data object Paladin : CharacterClass(Die.D10)
//    data object Ranger : CharacterClass(Die.D10)
//    data object Rogue : CharacterClass(Die.D8)
//    data object Warlock : CharacterClass(Die.D8)
//    data object Monk : CharacterClass(Die.D8)
//    data object Sorcerer : CharacterClass(Die.D6)
//    data object Bard : CharacterClass(Die.D8, WeaponProficiencies.simple)
//    data object Wizard : CharacterClass(Die.D8)
}

// missing classes
// Ranger, Rogue, Monk, Sorcerer, Wizard

class Fighter : CharacterClass(
    hitDie = Die.D10,
    weaponProficiency = WeaponProficiencies.all,
    isCriticalHit = { it.value >= 19 }
)
class Cleric : CharacterClass(Die.D8, WeaponProficiencies.simple)
class Druid : CharacterClass(Die.D8, WeaponProficiencies.simple)
class Barbarian : CharacterClass(Die.D12, WeaponProficiencies.all)
class Paladin : CharacterClass(Die.D10)
class Warlock : CharacterClass(Die.D8)
class Bard : CharacterClass(Die.D8, WeaponProficiencies.simple)