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

typealias WeaponProficiency = (Weapon) -> Boolean

// TODO category is also coming from weapon, do we need both?
internal object WeaponProficiencies {
    val all: WeaponProficiency = { _ -> true }
    val simple: WeaponProficiency = { weapon -> weapon.category == WeaponCategory.Simple }
    val none: WeaponProficiency = { _ -> false }
}

data class Weapon(
    val name: String,
    val category: WeaponCategory,
    val attackType: AttackType,
    val damageType: DamageType,
    private val whichStat: StatQuery,
    private val damageRoll: DamageRoll,
    private val rangeChecker: RangeChecker = { RangeClassification.OutOfRange },
) {
    fun whichStat(statBlock: StatBlock): Stat = whichStat(statBlock)

    fun dealDamage(statProvider: StatProvider, isCritical: Boolean): Int {
        val modifier = statProvider(whichStat)
        val rolledDamage = damageRoll.roll(isCritical)

        return rolledDamage + modifier.modifier
    }

    fun isTargetInRange(distance: Double): RangeClassification =
        rangeChecker(distance)
}

object Weapons {
    val LONGSWORD = Weapon(
        name = "Longsword",
        category = WeaponCategory.Martial,
        attackType = AttackType.Melee,
        damageType = DamageType.Slashing,
        whichStat = StatQueries.Str,
        damageRoll = SimpleDamageRoll(1, Die.D8)
    )

    val Shortbow = Weapon(
        name = "Shortbow",
        category = WeaponCategory.Simple,
        attackType = AttackType.Ranged,
        damageType = DamageType.Piercing,
        whichStat = StatQueries.Dex,
        damageRoll = SimpleDamageRoll(1, Die.D6),
        rangeChecker = RangeCheckers.ranged(normalRange = 80.0, longRange = 320.0)
    )
}
