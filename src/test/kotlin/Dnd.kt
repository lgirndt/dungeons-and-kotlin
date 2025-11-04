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

val SOME_CHARACTER = Character(
    name = "My Name",
    characterClass = CharacterClass.Fighter,
    stats = SOME_STAT_BOCK.copy(),
    level = 1,
    damageModifiers = DamageModifiers.NONE,
    currentWeapon = null,
    hitPoints = 10,
    armour =  { 10 },
)

val SOME_WEAPON = Weapon(
    name = "Surgebinder",
    attackType = AttackType.Melee,
    damageType = DamageType.Slashing,
    modifierStrategy = StrengthModifierStrategy(),
    damageRoll = SimpleDamageRoll(1, Die.D8),
)

val SOME_DAMAGE_MODIFIERS = DamageModifiers(
    resistances = emptySet(),
    immunities = emptySet(),
    vulnerabilities = emptySet(),
)

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