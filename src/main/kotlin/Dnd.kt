package org.example

import org.example.Die.Companion.D20


data  class Stat(val value: UInt) {
    val modifier : Int
        get() = value.toInt() / 2 - 5
}

data class StatBlock(
    val str : Stat,
    val dex : Stat,
    val con : Stat,
    val int : Stat,
    val wis : Stat,
    val cha : Stat,
) {
    constructor(
        str: UInt,
        dex: UInt,
        con: UInt,
        int: UInt,
        wis: UInt,
        cha: UInt)
            : this(
        Stat(str),
        Stat(dex),
        Stat(con),
        Stat(int),
        Stat(wis),
        Stat(cha),
    )

    companion object {}
}

sealed class CharacterClass(
    val hitDie: Die
)  {
   val name: String
        get() = this.javaClass.simpleName

    object Fighter: CharacterClass(Die.D10)
    object Cleric: CharacterClass(Die.D8)
    object Druid: CharacterClass(Die.D8)
    object Barbarian: CharacterClass(Die.D12)
    object Paladin: CharacterClass(Die.D10)
    object Ranger: CharacterClass(Die.D10)
    object Rogue: CharacterClass(Die.D8)
    object Warlock: CharacterClass(Die.D8)
    object Monk: CharacterClass(Die.D8)
    object Sorcerer: CharacterClass(Die.D6)
    object Bard: CharacterClass(Die.D8)
    object Wizard: CharacterClass(Die.D8)
}

data class DamageModifiers(
    val resistances: Set<DamageType> = emptySet(),
    val immunities: Set<DamageType> = emptySet(),
    val vulnerabilities: Set<DamageType> = emptySet(),
) {
    companion object {
        val NONE = DamageModifiers()
    }
}

interface Attackable {
    val armourClass: Int
    fun receiveDamage(amount: Int, damageType: DamageType) : Int
}

data class AttackOutcome(
    val hasBeenHit: Boolean,
    val damageDealt: Int,
    val hitRoll: Int = 0,
) {
    companion object {
        val MISS = AttackOutcome(false, 0)
    }
}

data class Character(
    val name: String,
    val characterClass: CharacterClass,
    val stats : StatBlock,
    val level: Int = 1,
    val damageModifiers: DamageModifiers = DamageModifiers.NONE,
    var currentWeapon: Weapon? = null,
    var hitPoints: Int,
    val armour : ((StatBlock) -> Int)
) : Attackable {
    companion object

    val proficiencyBonus: Int
        get() = 1 + (level - 1) / 4

    override val armourClass: Int
        get() = armour(stats)

    fun equip(weapon: Weapon) {
        this.currentWeapon = weapon
    }

    fun attack(opponent: Attackable, diceRoller: DiceRoller) : AttackOutcome {
        val currentWeapon = this.currentWeapon ?: return AttackOutcome.MISS
        val hitRoll = rollToHit(currentWeapon, diceRoller)
        if(hitRoll >= opponent.armourClass) {
            // TODO
            val damage = 10;
            val receivedDamage = opponent.receiveDamage(damage, currentWeapon.damageType)
            return AttackOutcome(true, receivedDamage, hitRoll)
        }
        return AttackOutcome(false, 0, hitRoll)
    }

    override fun receiveDamage(amount: Int, damageType: DamageType) : Int {
        // TODO
        return 0
    }

    private fun isProficientWith(weapon: Weapon): Boolean {
        // TODO
        return true
    }

    private fun rollToHit(currentWeapon: Weapon, diceRoller: DiceRoller): Int {
        val modifier = currentWeapon.receiveModifier(stats)
        val proficiencyModifier = if(isProficientWith(currentWeapon)) proficiencyBonus else 0
        val attackRoll = diceRoller.rollDie(D20) + modifier + proficiencyModifier

        return attackRoll
    }
}

data class Die(val numberOfFaces: Int) {
    companion object {
        val D6 = Die(6)
        val D8 = Die(8)
        val D10 = Die(10)
        val D12 = Die(12)
        val D20 = Die(20)
    }
}

interface DiceRoller {
    fun rollDie(die: Die): Int
}

enum class DamageType {
    Slashing,
    Piercing,
    Bludgeoning,
    Fire,
    Cold,
    Lightning,
    Acid,
    Poison,
    Psychic,
    Necrotic,
    Radiant,
    Thunder,
    Force,
}

enum class AttackType {
    Melee,
    Ranged,
}

interface WeaponModifierStrategy {
    fun getModifier(statBlock: StatBlock): Stat
}

private class StrengthModifierStrategy : WeaponModifierStrategy {
    override fun getModifier(statBlock: StatBlock): Stat {
        return statBlock.str
    }
}

sealed class Weapon {

    abstract val name: String
    abstract val attackType: AttackType
    abstract val damageType: DamageType

    abstract fun receiveModifier(statBlock: StatBlock): Int

    companion object {
        val LONGSWORD = WeaponHolder(
            name = "Longsword",
            attackType = AttackType.Melee,
            damageType = DamageType.Slashing,
            modifierStrategy = StrengthModifierStrategy(),
        )
    }

    data class WeaponHolder(
        override val name: String,
        override val attackType: AttackType,
        override val damageType: DamageType,
        private val modifierStrategy: WeaponModifierStrategy,
    ) : Weapon() {

        override fun receiveModifier(statBlock: StatBlock): Int {
            return modifierStrategy.getModifier(statBlock).modifier
        }

    }
}

class Armours {
    companion object {
        val CHAIN_MAIL = { _: StatBlock -> 16 }
        val LEATHER_ARMOUR = { stats: StatBlock -> 11 + stats.dex.value.toInt() }
    }
}

