package org.example

open class CharacterClass(
    private val hitDie: Die,
    private val weaponProficiency: WeaponProficiency = WeaponProficiencies.none,
    val isCriticalHit: (DieRoll) -> Boolean = { roll -> roll.value == 20 }
) {
    val name: String
        get() = this::class.simpleName!!

    fun isProficientWith(weapon: Weapon): Boolean = weaponProficiency(weapon.category, weapon)
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