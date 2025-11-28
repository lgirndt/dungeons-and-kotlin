package io.dungeons.domain

import io.dungeons.domain.world.Feet

enum class WeaponCategory {
    Simple,
    Martial,
}

enum class RangeClassification {
    WithinNormalRange,
    WithinLongRange,
    OutOfRange,
}

typealias RangeChecker = (distance: Feet) -> RangeClassification

object RangeCheckers {
    fun melee(maxMeleeRange: Feet): RangeChecker = { distance ->
        if (distance <= maxMeleeRange) {
            RangeClassification.WithinNormalRange
        } else {
            RangeClassification.OutOfRange
        }
    }

    fun ranged(normalRange: Feet, longRange: Feet): RangeChecker = { distance ->
        when {
            distance <= normalRange -> RangeClassification.WithinNormalRange
            distance <= longRange -> RangeClassification.WithinLongRange
            else -> RangeClassification.OutOfRange
        }
    }
}

typealias WeaponProficiency = (Weapon) -> Boolean

internal object WeaponProficiencies {
    val all: WeaponProficiency = { _ -> true }
    val simple: WeaponProficiency = { weapon -> weapon.category == WeaponCategory.Simple }
    val none: WeaponProficiency = { _ -> false }
}

abstract class AttackSource {
    abstract val name: String
    abstract val damageType: DamageType
    protected abstract val statQuery: StatQuery
    protected abstract val damageRoll: DamageRoll
    protected abstract val rangeChecker: RangeChecker

    fun isTargetInRange(distance: Feet): RangeClassification = rangeChecker(distance)

    fun dealDamage(statProvider: StatProvider, isCritical: Boolean): Int {
        val modifier = statProvider(statQuery)
        val rolledDamage = damageRoll.roll(isCritical)

        return rolledDamage + modifier.modifier
    }

    fun whichStat(statBlock: StatBlock): Stat = statQuery(statBlock)
}

data class Weapon(
    override val name: String,
    override val damageType: DamageType,
    override val statQuery: StatQuery,
    override val damageRoll: DamageRoll,
    override val rangeChecker: RangeChecker = { RangeClassification.OutOfRange },
    val category: WeaponCategory,
) : AttackSource()

object Weapons {
    val LONGSWORD = Weapon(
        name = "Longsword",
        category = WeaponCategory.Martial,
        damageType = DamageType.Slashing,
        statQuery = StatQueries.Str,
        damageRoll = SimpleDamageRoll(1, Die.D8),
        rangeChecker = RangeCheckers.melee(Feet(5.0)),
    )

    val Shortbow = Weapon(
        name = "Shortbow",
        category = WeaponCategory.Simple,
        damageType = DamageType.Piercing,
        statQuery = StatQueries.Dex,
        damageRoll = SimpleDamageRoll(1, Die.D6),
        rangeChecker = RangeCheckers.ranged(normalRange = Feet(80.0), longRange = Feet(320.0)),
    )

    val Dagger = Weapon(
        name = "Dagger",
        category = WeaponCategory.Simple,
        damageType = DamageType.Piercing,
        statQuery = StatQueries.Str,
        damageRoll = SimpleDamageRoll(1, Die.D4),
        rangeChecker = RangeCheckers.melee(Feet(5.0)),
    )
}
