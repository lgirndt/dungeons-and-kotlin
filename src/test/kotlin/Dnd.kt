import com.google.common.collect.ImmutableListMultimap
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.example.*
import org.junit.jupiter.api.assertNotNull

const val DEFAULT_STAT_VALUE = 10

val SOME_STAT_BOCK = StatBlock(
    str = Stat(DEFAULT_STAT_VALUE),
    dex = Stat(DEFAULT_STAT_VALUE),
    con = Stat(DEFAULT_STAT_VALUE),
    int = Stat(DEFAULT_STAT_VALUE),
    wis = Stat(DEFAULT_STAT_VALUE),
    cha = Stat(DEFAULT_STAT_VALUE),
)

fun StatBlock.copyByInts(
    str: Int = this.str.toInt(),
    dex: Int = this.dex.toInt(),
    con: Int = this.con.toInt(),
    int: Int = this.int.toInt(),
    wis: Int = this.wis.toInt(),
    cha: Int = this.cha.toInt(),
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

fun StatBlock.Companion.fromModifiers(
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
    modifierStrategy = StrengthModifierStrategy,
    damageRoll = SimpleDamageRoll(1, Die.D8),
)

val SOME_DAMAGE_MODIFIERS = DamageModifiers(
    resistances = emptySet(),
    immunities = emptySet(),
    vulnerabilities = emptySet(),
)

data class DieRoll(val die: Die, val result: Int)

infix fun Die.rolls(result: Int) = DieRoll(this, result)

inline fun withFixedDice(
    vararg expectedRolls: DieRoll,
    runWithFixedDice : () -> Unit
) {
    val multimap = ImmutableListMultimap.builder<Die, Int>().apply {
        expectedRolls.forEach { put(it.die, it.result) }
    }.build()

    val mockedDice = mutableListOf<Die>()
    try {
        // expect
        multimap.asMap().forEach { (die, allRolls) ->
            mockkObject(die)
            every { die.roll() } returnsMany allRolls.toList()
            mockedDice.add(die)
        }

        runWithFixedDice()

        // verify
        multimap.asMap().forEach { (die, allRolls) ->
            verify(exactly = allRolls.size) { die.roll() }
        }

    } finally {
        mockedDice.forEach { verify(exactly = 1) { it.roll() } }
    }
}