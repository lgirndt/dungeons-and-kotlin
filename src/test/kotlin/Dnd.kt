import com.google.common.collect.ImmutableListMultimap
import io.mockk.every
import org.example.*

const val DEFAULT_STAT_VALUE = 10

fun StatBlock.Companion.create(
    str: Int = DEFAULT_STAT_VALUE,
    dex: Int = DEFAULT_STAT_VALUE,
    con: Int = DEFAULT_STAT_VALUE,
    int: Int = DEFAULT_STAT_VALUE,
    wis: Int = DEFAULT_STAT_VALUE,
    cha: Int = DEFAULT_STAT_VALUE,
): StatBlock {
    return StatBlock(str, dex, con, int, wis, cha)
}

fun StatBlock.Companion.createWithModifiers(
    strMod: Int = 0,
    dexMod: Int = 0,
    conMod: Int = 0,
    intMod: Int = 0,
    wisMod: Int = 0,
    chaMod: Int = 0,
): StatBlock {
    return StatBlock(
        DEFAULT_STAT_VALUE + strMod * 2,
        DEFAULT_STAT_VALUE + dexMod * 2,
        DEFAULT_STAT_VALUE + conMod * 2,
        DEFAULT_STAT_VALUE + intMod * 2,
        DEFAULT_STAT_VALUE + wisMod * 2,
        DEFAULT_STAT_VALUE + chaMod * 2,
    )
}

fun Character.Companion.create(
    name: String = "My Name",
    characterClass: CharacterClass = CharacterClass.Fighter,
    stats: StatBlock = StatBlock.create(),
    level: Int = 1,
    damageModifiers: DamageModifiers = DamageModifiers.NONE,
    currentWeapon: Weapon? = null,
    hitPoints: Int = 10,
    armour: (StatBlock) -> Int = { 10 },
): Character {
    return Character(name, characterClass, stats, level, damageModifiers, currentWeapon, hitPoints, armour)
}

fun Weapon.Companion.create(
    name: String = "Surgebinder",
    attackType: AttackType = AttackType.Melee,
    damageType: DamageType = DamageType.Slashing,
    modifierStrategy: WeaponModifierStrategy = StrengthModifierStrategy(),
    damageDie: Die = Die.D8,
): Weapon {
    return Weapon(name, attackType, damageType, modifierStrategy, SimpleDamageRoll(1, damageDie))
}

fun DamageModifiers.Companion.create(
    resistances: Set<DamageType> = emptySet(),
    immunities: Set<DamageType> = emptySet(),
    vulnerabilities: Set<DamageType> = emptySet(),
): DamageModifiers {
    return DamageModifiers(resistances, immunities, vulnerabilities)
}

data class DieRoll(val die: Die, val result: Int)

infix fun Die.rolls(result: Int) = DieRoll(this, result)

fun expectDiceRolls(
    diceRoller: DiceRoller,
    vararg expectedRolls: DieRoll
) {
    val multimap = expectedRolls.fold(ImmutableListMultimap.builder<Die, Int>()) { builder, dieRoll ->
        builder.put(dieRoll.die, dieRoll.result)
    }.build()

    for (die in multimap.keySet()) {
        val results: List<Int> = multimap.get(die)
        every { diceRoller.rollDie(die) } returnsMany results
    }
}