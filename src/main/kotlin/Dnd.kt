package org.example

import org.example.Die.Companion.D20

@JvmInline
value class Stat(private val value: Int) {
    val modifier: Int
        get() = value / 2 - 5

    fun toInt(): Int = value
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
        get() = this::class.simpleName!!

    data object Fighter : CharacterClass(Die.D10)
    data object Cleric : CharacterClass(Die.D8)
    data object Druid : CharacterClass(Die.D8)
    data object Barbarian : CharacterClass(Die.D12)
    data object Paladin : CharacterClass(Die.D10)
    data object Ranger : CharacterClass(Die.D10)
    data object Rogue : CharacterClass(Die.D8)
    data object Warlock : CharacterClass(Die.D8)
    data object Monk : CharacterClass(Die.D8)
    data object Sorcerer : CharacterClass(Die.D6)
    data object Bard : CharacterClass(Die.D8)
    data object Wizard : CharacterClass(Die.D8)
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

typealias WeaponModifierStrategy = (StatBlock) -> Stat

val StrengthModifierStrategy : WeaponModifierStrategy = StatBlock::str

interface DamageRoll {
    fun roll(isCritical: Boolean): Int
}

class SimpleDamageRoll(
    private val numberOfDice: Int,
    private val die: Die,
    private val bonus: Int = 0,
) : DamageRoll {
    override fun roll(isCritical: Boolean): Int {
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

    fun receiveModifier(statBlock: StatBlock): Int =
        modifierStrategy(statBlock).modifier

    fun dealDamage(stats: StatBlock, isCritical: Boolean): Int {
        val modifier = modifierStrategy(stats)
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
    val LEATHER_ARMOUR = { stats: StatBlock -> 11 + stats.dex.toInt() }
}

