import io.dungeons.Die.Companion.D8
import io.dungeons.RangeClassification
import io.dungeons.Weapons
import io.dungeons.world.Feet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class WeaponTest {
    @Test
    fun `a weapon deals proper damage`() {
        val longsword = Weapons.LONGSWORD
        val stats = SOME_STAT_BOCK.copyByInts(str = 16)

        withFixedDice(D8 rolls 6) {
            val damage = longsword.dealDamage({ query -> query(stats) }, false)

            assertEquals(6 + 3, damage) // 3 is the modifier for str 16
        }
    }

    @Test
    fun `a crit deals double damage`() {
        val longsword = Weapons.LONGSWORD
        val stats = SOME_STAT_BOCK.copyByInts(str = 16)

        withFixedDice(
            D8 rolls 4,
            D8 rolls 7,
        ) {
            val damage = longsword.dealDamage({ query -> query(stats) }, true)

            assertEquals(4 + 7 + 3, damage) // 3 is the modifier for str 16
        }
    }
}

class RangeCheckersTest {
    @Test
    fun `melee range checker works correctly`() {
        val meleeChecker = io.dungeons.RangeCheckers.melee(Feet(2.0))

        assertEquals(RangeClassification.WithinNormalRange, meleeChecker(Feet(1.5)))
        assertEquals(RangeClassification.WithinNormalRange, meleeChecker(Feet(2.0)))
        assertEquals(RangeClassification.OutOfRange, meleeChecker(Feet(2.5)))
    }
}
