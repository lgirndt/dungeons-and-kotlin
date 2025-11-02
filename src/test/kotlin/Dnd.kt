import org.example.Character
import org.example.CharacterClass
import org.example.DamageModifiers
import org.example.StatBlock
import org.example.Weapon

const val DEFAULT_STAT_VALUE = 10u

fun StatBlock.Companion.create(
    str: UInt = DEFAULT_STAT_VALUE,
    dex: UInt = DEFAULT_STAT_VALUE,
    con: UInt = DEFAULT_STAT_VALUE,
    int: UInt = DEFAULT_STAT_VALUE,
    wis: UInt = DEFAULT_STAT_VALUE,
    cha: UInt = DEFAULT_STAT_VALUE,
): StatBlock {
    return StatBlock(str, dex, con, int, wis, cha)
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
