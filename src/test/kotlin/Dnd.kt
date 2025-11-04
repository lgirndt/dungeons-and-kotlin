import DEFAULT_STAT_VALUE
import com.google.common.collect.ImmutableListMultimap
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.example.*

const val DEFAULT_STAT_VALUE = 10

val SOME_STAT_BOCK = StatBlock(
    str = Stat(DEFAULT_STAT_VALUE),
    dex = Stat(DEFAULT_STAT_VALUE),
    con = Stat(DEFAULT_STAT_VALUE),
    int = Stat(DEFAULT_STAT_VALUE),
    wis = Stat(DEFAULT_STAT_VALUE),
    cha = Stat(DEFAULT_STAT_VALUE),
)

fun StatBlock.copy(
    str: Int = this.str.value,
    dex: Int = this.dex.value,
    con: Int = this.con.value,
    int: Int = this.int.value,
    wis: Int = this.wis.value,
    cha: Int = this.cha.value,
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

fun StatBlock.Companion.createWithModifiers(
    strMod: Int = 0,
    dexMod: Int = 0,
    conMod: Int = 0,
    intMod: Int = 0,
    wisMod: Int = 0,
    chaMod: Int = 0,
): StatBlock {
    return StatBlock(
        Stat(DEFAULT_STAT_VALUE + strMod * 2),
        Stat(DEFAULT_STAT_VALUE + dexMod * 2),
        Stat(DEFAULT_STAT_VALUE + conMod * 2),
        Stat(DEFAULT_STAT_VALUE + intMod * 2),
        Stat(DEFAULT_STAT_VALUE + wisMod * 2),
        Stat(DEFAULT_STAT_VALUE + chaMod * 2),
    )
}

fun Character.Companion.create(
    name: String = "My Name",
    characterClass: CharacterClass = CharacterClass.Fighter,
    stats: StatBlock = SOME_STAT_BOCK.copy(),
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

fun withFixedDice(
    vararg expectedRolls: DieRoll,
    runWithFixedDice : () -> Unit
) {
    val multimap = expectedRolls.fold(ImmutableListMultimap.builder<Die, Int>()) { builder, dieRoll ->
        builder.put(dieRoll.die, dieRoll.result)
    }.build()

    val mockedDice = mutableListOf<Die>()
    try {
        // expect
        for (die in multimap.keySet()) {
            mockkObject(die)
            val allRolls = multimap.get(die)
            every { die.roll() } returnsMany allRolls
            mockedDice.add(die)
        }
        runWithFixedDice()

        // verify
        for (die in multimap.keySet()) {
            val allRolls = multimap.get(die)
            io.mockk.verify(exactly = allRolls.size) { die.roll() }
        }

    } finally {
        for (die in mockedDice) {
            unmockkObject(die)
        }
    }
}