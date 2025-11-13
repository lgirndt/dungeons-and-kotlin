package io.dungeons

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

typealias StatQuery = (StatBlock) -> Stat
object StatQueries {
    val Str: StatQuery = { stats: StatBlock -> stats.str }
    val Dex: StatQuery = { stats: StatBlock -> stats.dex }
    val Con: StatQuery = { stats: StatBlock -> stats.con }
    val Int: StatQuery = { stats: StatBlock -> stats.int }
    val Wis: StatQuery = { stats: StatBlock -> stats.wis }
    val Cha: StatQuery = { stats: StatBlock -> stats.cha }
}

typealias StatProvider = (StatQuery) -> Stat

data class DamageModifiers(
    val resistances: Set<DamageType> = emptySet(),
    val immunities: Set<DamageType> = emptySet(),
    val vulnerabilities: Set<DamageType> = emptySet(),
) {
    companion object {
        val NONE = DamageModifiers()
    }
}

enum class RollModifier {
    ADVANTAGE,
    DISADVANTAGE,
    NORMAL;

    fun roll(die: Die): DieRoll {
        return when (this) {
            ADVANTAGE -> maxOf(die.roll(), die.roll())
            DISADVANTAGE -> minOf(die.roll(), die.roll())
            NORMAL -> die.roll()
        }
    }

    fun giveAdvantage(): RollModifier = when (this) {
        NORMAL -> ADVANTAGE
        DISADVANTAGE -> NORMAL
        ADVANTAGE -> ADVANTAGE
    }

    fun giveDisadvantage(): RollModifier = when (this) {
        NORMAL -> DISADVANTAGE
        ADVANTAGE -> NORMAL
        DISADVANTAGE -> DISADVANTAGE
    }
}

data class DieRoll(val die: Die, val value: Int) : Comparable<DieRoll> {
    override fun compareTo(other: DieRoll): Int = compareBy(DieRoll::value).compare(this, other)


}

class Die private constructor(val numberOfFaces: Int) {

    fun roll(): DieRoll {
        return DieRoll(this, (1..numberOfFaces).random())
    }

    override fun toString(): String {
        return "D$numberOfFaces"
    }

    companion object {
        val D4 = Die(4)
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
            total + die.roll().value
        }
    }
}

object Armours {

    val CHAIN_MAIL = { _: StatBlock -> 16 }
    val LEATHER_ARMOUR = { stats: StatBlock -> 11 + stats.dex.toInt() }
}

@JvmInline
value class ProficiencyBonus private constructor(private val value: Int) {
    fun toInt(): Int = value
    companion object {
        fun fromLevel(level: Int) : ProficiencyBonus = ProficiencyBonus(1 + (level - 1) / 4)
        val None = ProficiencyBonus(0)
    }
}

class AbilityCheckResult(
    val isSuccessful: Boolean,
) {

    fun onSuccess(action: () -> Unit): AbilityCheckResult {
        if (isSuccessful) {
            action()
        }
        return this
    }
    fun onFailure(action: () -> Unit): AbilityCheckResult {
        if (!isSuccessful) {
            action()
        }
        return this
    }
}