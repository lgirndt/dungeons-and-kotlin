import com.google.common.collect.ImmutableListMultimap
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.example.*
import javax.swing.text.Position

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

fun PlayerCharacter.Companion.aPlayerCharacter(
    name: String = "My Name",
    stats: StatBlock = SOME_STAT_BOCK.copy(),
    hitPoints: Int = 10,
    armourClass: Int = 12,
    damageModifiers: DamageModifiers = DamageModifiers.NONE,
    position: Coordinate = Coordinate(0,0),
    level: Int = 1,
    weapon: Weapon = SOME_WEAPON.copy(),
    classFeatures : CharacterClass = Fighter(),
) : PlayerCharacter {
    return PlayerCharacter(
        core = CoreEntityData(
            name = name,
            stats = stats,
            hitPoints = hitPoints,
            armourClass = armourClass,
            damageModifiers = damageModifiers,
            position = position,
        ),
        data = PlayerCharacterData(
            level = level,
            weapon = weapon
        ),
        classFeatures = classFeatures
    )
}


val SOME_WEAPON = PhysicalWeapon(
    name = "Surgebinder",
    category = WeaponCategory.Martial,
    damageType = DamageType.Slashing,
    statQuery = StatQueries.Str,
    damageRoll = SimpleDamageRoll(1, Die.D8),
    rangeChecker = RangeCheckers.melee(5.0)
)

val SOME_CHARACTER = Character(
    name = "My Name",
    characterClass = Fighter(),
    stats = SOME_STAT_BOCK.copy(),
    level = 1,
    damageModifiers = DamageModifiers.NONE,
    currentWeapon = null,
    hitPoints = 10,
    armour = { 10 },
)

val SOME_DAMAGE_MODIFIERS = DamageModifiers(
    resistances = emptySet(),
    immunities = emptySet(),
    vulnerabilities = emptySet(),
)

infix fun Die.rolls(result: Int) = DieRoll(this, result)

inline fun withFixedDice(
    vararg expectedRolls: DieRoll,
    runWithFixedDice: () -> Unit
) {
    val multimap = ImmutableListMultimap.builder<Die, Int>().apply {
        expectedRolls.forEach { put(it.die, it.value) }
    }.build()

    val mockedDice = multimap.asMap().map { (die, allRolls) ->
        // TODO more functional
        val allDieRolls = allRolls.map { DieRoll(die, it) }
        die.also {
            mockkObject(it)
            every { it.roll() } returnsMany allDieRolls.toList()
        }
    }

    try {
        runWithFixedDice()
    } finally {
        // verify
        multimap.asMap().forEach { (die, allRolls) ->
            verify(exactly = allRolls.size) { die.roll() }
        }

        mockedDice.forEach { unmockkObject(it) }
    }
}