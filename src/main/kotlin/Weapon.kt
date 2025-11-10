package org.example

enum class WeaponCategory {
    Simple,
    Martial,
}

enum class RangeClassification {
    WithinNormalRange,
    WithinLongRange,
    OutOfRange,
}

typealias RangeChecker = (distance: Double) -> RangeClassification

object RangeCheckers {
    fun melee(maxMeleeRange: Double): RangeChecker = { distance ->
        if (distance <= maxMeleeRange) {
            RangeClassification.WithinNormalRange
        } else {
            RangeClassification.OutOfRange
        }
    }

    fun ranged(normalRange: Double, longRange: Double): RangeChecker = { distance ->
        when {
            distance <= normalRange -> RangeClassification.WithinNormalRange
            distance <= longRange -> RangeClassification.WithinLongRange
            else -> RangeClassification.OutOfRange
        }
    }
}

typealias WeaponProficiency = (AttackSource) -> Boolean

internal object WeaponProficiencies {
    val all: WeaponProficiency = { _ -> true }
    val simple: WeaponProficiency = { weapon -> weapon.category == WeaponCategory.Simple }
    val none: WeaponProficiency = { _ -> false }
}

abstract class AttackSource {
    abstract val name: String
    abstract val category: WeaponCategory
    abstract val damageType: DamageType
    protected abstract val statQuery: StatQuery
    protected abstract val damageRoll: DamageRoll
    protected abstract val rangeChecker: RangeChecker

    fun isTargetInRange(distance: Double): RangeClassification = rangeChecker(distance)

    fun dealDamage(statProvider: StatProvider, isCritical: Boolean): Int {
        val modifier = statProvider(statQuery)
        val rolledDamage = damageRoll.roll(isCritical)

        return rolledDamage + modifier.modifier
    }

    fun whichStat(statBlock: StatBlock): Stat = statQuery(statBlock)
}

data class Weapon(
    override val name: String,
    override val category: WeaponCategory,
    override val damageType: DamageType,
    override val statQuery: StatQuery,
    override val damageRoll: DamageRoll,
    override val rangeChecker: RangeChecker = { RangeClassification.OutOfRange },
) : AttackSource()

object Weapons {
    val LONGSWORD = Weapon(
        name = "Longsword",
        category = WeaponCategory.Martial,
        damageType = DamageType.Slashing,
        statQuery = StatQueries.Str,
        damageRoll = SimpleDamageRoll(1, Die.D8)
    )

    val Shortbow = Weapon(
        name = "Shortbow",
        category = WeaponCategory.Simple,
        damageType = DamageType.Piercing,
        statQuery = StatQueries.Dex,
        damageRoll = SimpleDamageRoll(1, Die.D6),
        rangeChecker = RangeCheckers.ranged(normalRange = 80.0, longRange = 320.0)
    )
}
