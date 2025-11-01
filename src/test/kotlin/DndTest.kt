import org.example.Character
import org.example.CharacterClass
import org.example.Dnd2025CharacterClass
import org.example.Dnd2025CharacterClass.Barbarian
import org.example.Dnd2025CharacterClass.Warlock
import org.example.Stat
import org.example.StatBlock
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

const val DEFAULT_STAT_VALUE = 10u;

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
    characterClass: CharacterClass = Dnd2025CharacterClass.Fighter,
    stats : StatBlock = StatBlock.create(),
    level: Int = 1,
) : Character {
    return Character(name, characterClass, stats, level)
}

class DndTest {
    @Test
    fun `a modifier should return the correct value`() {
        assertAll(
            { assertEquals(-1, Stat(8u).modifier) },
            { assertEquals(-1, Stat(9u).modifier) },

            { assertEquals(0, Stat(10u).modifier) },
            { assertEquals(0, Stat(11u).modifier) },

            { assertEquals(1, Stat(12u).modifier) },
            { assertEquals(1, Stat(13u).modifier) },
        )
    }

    @Test
    fun `overloaded ctor should assign values correctly`() {
        val statBlocks = StatBlock(10u, 11u, 12u, 13u, 14u, 15u)
        assertAll(
            { assertEquals(10u, statBlocks.str.value) },
            { assertEquals(11u, statBlocks.dex.value) },
            { assertEquals(12u, statBlocks.con.value) },
            { assertEquals(13u, statBlocks.int.value) },
            { assertEquals(14u, statBlocks.wis.value) },
            { assertEquals(15u, statBlocks.cha.value) },
        )
    }

    @Test
    fun `some StatBlock`() {
        val dexBlock = StatBlock.create(dex = 12u)
        assertAll(
            { assertEquals(12u, dexBlock.dex.value) },
            { assertEquals(DEFAULT_STAT_VALUE, dexBlock.str.value) },
        )
    }

    @Test
    fun `CharacterClasses have the proper name`() {
        assertEquals("Barbarian", Barbarian.name)
    }

    @Test
    fun `create a Warlock`() {
        val warlock = Character.create(characterClass = Warlock)
        assertEquals(Warlock, warlock.characterClass)
    }

    @Test
    fun `a character with custom stats and class`() {
        val myCharacter = Character.create(
            characterClass = Warlock,
            stats = StatBlock.create(dex = 12u, con = 14u),
        )
        assertAll(
            { assertEquals(12u, myCharacter.stats.dex.value) },
            { assertEquals(Warlock, myCharacter.characterClass) },
        )
    }
}

