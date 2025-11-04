package org.example

import org.example.Die.Companion.D20
import kotlin.math.max

@JvmInline
value class Stat(val value: Int) {
    val modifier: Int
        get() = value / 2 - 5
}

data class StatBlock(
    val str: Stat,
    val dex: Stat,
    val con: Stat,
    val int: Stat,
    val wis: Stat,
    val cha: Stat,
) {

    companion object {
        fun create(
            str: Int,
            dex: Int,
            con: Int,
            int: Int,
            wis: Int,
            cha: Int,
        ): StatBlock {
            return StatBlock(
                Stat(str),
                Stat(dex),
                Stat(con),
                Stat(int),
                Stat(wis),
                Stat(cha),
            )
        }
    }
}

sealed class CharacterClass(
    val hitDie: Die
) {
    val name: String
        get() = this.javaClass.simpleName

    object Fighter : CharacterClass(Die.D10)
    object Cleric : CharacterClass(Die.D8)
    object Druid : CharacterClass(Die.D8)
    object Barbarian : CharacterClass(Die.D12)
    object Paladin : CharacterClass(Die.D10)
    object Ranger : CharacterClass(Die.D10)
    object Rogue : CharacterClass(Die.D8)
    object Warlock : CharacterClass(Die.D8)
    object Monk : CharacterClass(Die.D8)
    object Sorcerer : CharacterClass(Die.D6)
    object Bard : CharacterClass(Die.D8)
    object Wizard : CharacterClass(Die.D8)
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
    fun receiveDamage(amount: Int, damageType: DamageType): Int
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
    val stats: StatBlock,
    val level: Int = 1,
    val damageModifiers: DamageModifiers = DamageModifiers.NONE,
    var currentWeapon: Weapon? = null,
    var hitPoints: Int,
    val armour: ((StatBlock) -> Int)
) : Attackable {
    companion object

    val proficiencyBonus: Int
        get() = 1 + (level - 1) / 4

    override val armourClass: Int
        get() = armour(stats)

    fun equip(weapon: Weapon) {
        this.currentWeapon = weapon
    }

    fun attack(opponent: Attackable): AttackOutcome {
        // to hit
        val currentWeapon = this.currentWeapon ?: return AttackOutcome.MISS
        val modifier = currentWeapon.receiveModifier(stats)

        val hitRollD20 = D20.roll()
        val proficiencyModifier = if (isProficientWith(currentWeapon)) proficiencyBonus else 0
        val hitRoll = hitRollD20 + modifier + proficiencyModifier

        if (hitRoll >= opponent.armourClass) {
            // damage
            val isCritical = hitRollD20 == 20
            val damage = currentWeapon.dealDamage(stats, isCritical)
            val receivedDamage = opponent.receiveDamage(damage, currentWeapon.damageType)

            return AttackOutcome(true, receivedDamage, hitRoll)
        }
        return AttackOutcome(false, 0, hitRoll)
    }

    override fun receiveDamage(amount: Int, damageType: DamageType): Int {
        val adjustedAmount = when (damageType) {
            in damageModifiers.immunities -> 0
            in damageModifiers.resistances -> amount / 2
            in damageModifiers.vulnerabilities -> amount * 2
            else -> amount
        }

        hitPoints = max(hitPoints - adjustedAmount, 0)

        return adjustedAmount
    }

    private fun isProficientWith(weapon: Weapon): Boolean {
        // TODO
        return true
    }

}

class Die private constructor(val numberOfFaces: Int) {

    fun roll(): Int {
        return (1..numberOfFaces).random()
    }

    override fun toString(): String {
        return "D$numberOfFaces"
    }

    companion object {
        val D6 = Die(6)
        val D8 = Die(8)
        val D10 = Die(10)
        val D12 = Die(12)
        val D20 = Die(20)

        internal val ALL_DICE = listOf(D6, D8, D10, D12, D20)
    }
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

internal object StrengthModifierStrategy : WeaponModifierStrategy {
    override fun getModifier(statBlock: StatBlock): Stat {
        return statBlock.str
    }
}

interface DamageRoll {
    fun roll(isCritical: Boolean): Int
}

class SimpleDamageRoll(
    private val numberOfDice: Int,
    private val die: Die,
    private val bonus: Int = 0,
) : DamageRoll {
    override fun roll(isCritical: Boolean): Int {
        var total = bonus
        val critMultiplier = if (isCritical) 2 else 1
        return (1..(numberOfDice * critMultiplier)).fold(bonus) { total, _ ->
            total + die.roll()
        }
    }
}

data class Weapon(
    val name: String,
    val attackType: AttackType,
    val damageType: DamageType,
    private val modifierStrategy: WeaponModifierStrategy,
    private val damageRoll: DamageRoll
) {
    companion object

    fun receiveModifier(statBlock: StatBlock): Int {
        return modifierStrategy.getModifier(statBlock).modifier
    }

    fun dealDamage(stats: StatBlock, isCritical: Boolean): Int {
        val modifier = modifierStrategy.getModifier(stats)
        val rolledDamage = damageRoll.roll(isCritical)

        return rolledDamage + modifier.modifier
    }
}

object Weapons {
    val LONGSWORD = Weapon(
        name = "Longsword",
        attackType = AttackType.Melee,
        damageType = DamageType.Slashing,
        modifierStrategy = StrengthModifierStrategy,
        damageRoll = SimpleDamageRoll(1, Die.D8)
    )
}

object Armours {

    val CHAIN_MAIL = { _: StatBlock -> 16 }
    val LEATHER_ARMOUR = { stats: StatBlock -> 11 + stats.dex.value }
}

