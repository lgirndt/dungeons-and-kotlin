package org.example

typealias WeaponModifierStrategy = (StatBlock) -> Stat

internal val StrengthModifierStrategy : WeaponModifierStrategy = StatBlock::str

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
}

data class Weapon(
    val name: String,
    val category: WeaponCategory,
    val attackType: AttackType,
    val damageType: DamageType,
    private val modifierStrategy: WeaponModifierStrategy,
    private val damageRoll: DamageRoll,
    private val rangeChecker: RangeChecker = { RangeClassification.OutOfRange },
) {
    fun receiveModifier(statBlock: StatBlock): Int =
        modifierStrategy(statBlock).modifier

    fun dealDamage(stats: StatBlock, isCritical: Boolean): Int {
        val modifier = modifierStrategy(stats)
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
        modifierStrategy = StrengthModifierStrategy,
        damageRoll = SimpleDamageRoll(1, Die.D8)
    )
}
