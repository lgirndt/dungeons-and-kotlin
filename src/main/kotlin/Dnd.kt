package org.example

import org.example.Die.Companion.D20


data class Stat(val value: Int) {
    val modifier : Int
        get() = value / 2 - 5
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
        str: Int,
        dex: Int,
        con: Int,
        int: Int,
        wis: Int,
        cha: Int)
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
        val hitRollD20 = diceRoller.rollDie(D20)
        val modifier = currentWeapon.receiveModifier(stats)
        val proficiencyModifier = if (isProficientWith(currentWeapon)) proficiencyBonus else 0
        val hitRoll = hitRollD20 + modifier + proficiencyModifier
        if(hitRoll >= opponent.armourClass) {
            val isCritical = hitRollD20 == 20
            val damage = currentWeapon.dealDamage(stats, diceRoller, isCritical)
            val receivedDamage = opponent.receiveDamage(damage, currentWeapon.damageType)
            return AttackOutcome(true, receivedDamage, hitRoll)
        }
        return AttackOutcome(false, 0, hitRoll)
    }

    override fun receiveDamage(amount: Int, damageType: DamageType) : Int {
        // TODO
        return amount
    }

    private fun isProficientWith(weapon: Weapon): Boolean {
        // TODO
        return true
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

interface DamageRoll {
    fun roll(diceRoller: DiceRoller, isCritical: Boolean): Int
}

class SimpleDamageRoll(
    private val numberOfDice: Int,
    private val die: Die,
    private val bonus: Int = 0,
) : DamageRoll {
    override fun roll(diceRoller: DiceRoller, isCritical: Boolean): Int {
        var total = bonus
        val critMultiplier = if (isCritical) 2 else 1
        repeat(numberOfDice * critMultiplier) {
            total += diceRoller.rollDie(die)
        }
        return total
    }
}

sealed class Weapon {

    abstract val name: String
    abstract val attackType: AttackType
    abstract val damageType: DamageType

    abstract fun receiveModifier(statBlock: StatBlock): Int
    abstract fun dealDamage(stats: StatBlock, diceRoller: DiceRoller, isCritical: Boolean): Int

    companion object {
        val LONGSWORD = WeaponHolder(
            name = "Longsword",
            attackType = AttackType.Melee,
            damageType = DamageType.Slashing,
            modifierStrategy = StrengthModifierStrategy(),
            damageRoll = SimpleDamageRoll(1, Die.D8)
        )
    }

    data class WeaponHolder(
        override val name: String,
        override val attackType: AttackType,
        override val damageType: DamageType,
        private val modifierStrategy: WeaponModifierStrategy,
        private val damageRoll: DamageRoll
    ) : Weapon() {

        override fun receiveModifier(statBlock: StatBlock): Int {
            return modifierStrategy.getModifier(statBlock).modifier
        }

        override fun dealDamage(stats: StatBlock, diceRoller: DiceRoller, isCritical: Boolean): Int {
            val modifier = modifierStrategy.getModifier(stats)
            val rolledDamage = damageRoll.roll(diceRoller, isCritical)

            return rolledDamage + modifier.modifier
        }

    }
}

class Armours {
    companion object {
        val CHAIN_MAIL = { _: StatBlock -> 16 }
        val LEATHER_ARMOUR = { stats: StatBlock -> 11 + stats.dex.value }
    }
}

