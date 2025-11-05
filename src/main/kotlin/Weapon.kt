package org.example

typealias WeaponModifierStrategy = (StatBlock) -> Stat

internal val StrengthModifierStrategy : WeaponModifierStrategy = StatBlock::str

enum class WeaponCategory {
    SimpleMelee,
    SimpleRanged,
    MartialMelee,
    MartialRanged;

    companion object {
        val ALL = entries.toSet()
        val SIMPLE  = setOf(SimpleMelee, SimpleRanged)
        val MARTIAL = setOf(MartialMelee, MartialRanged)
    }
}

data class Weapon(
    val name: String,
    val category: WeaponCategory,
    val attackType: AttackType,
    val damageType: DamageType,
    private val modifierStrategy: WeaponModifierStrategy,
    private val damageRoll: DamageRoll
) {
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
        category = WeaponCategory.MartialMelee,
        attackType = AttackType.Melee,
        damageType = DamageType.Slashing,
        modifierStrategy = StrengthModifierStrategy,
        damageRoll = SimpleDamageRoll(1, Die.D8)
    )
}
