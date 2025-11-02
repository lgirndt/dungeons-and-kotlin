import com.google.common.collect.ImmutableListMultimap
import io.mockk.every
import org.example.Character
import org.example.CharacterClass
import org.example.DamageModifiers
import org.example.DiceRoller
import org.example.Die
import org.example.StatBlock
import org.example.Weapon
import kotlin.math.exp

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

data class DieRoll(val die: Die, val result: Int)

infix fun Die.rolls(result: Int) = DieRoll(this, result)

fun expectDiceRolls(
    diceRoller: DiceRoller,
    vararg expectedRolls: DieRoll
) {
    val multimap = expectedRolls.fold(ImmutableListMultimap.builder<Die, Int>()) { builder, dieRoll ->
        builder.put(dieRoll.die, dieRoll.result)
    }.build()

    for(die in  multimap.keySet()) {
        val results : List<Int> = multimap.get(die)
        every { diceRoller.rollDie(die) } returnsMany results
    }
}