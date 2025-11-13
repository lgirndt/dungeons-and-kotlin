import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.dungeons.Die.Companion.D8
import io.dungeons.Feet
import io.dungeons.RangeClassification
import io.dungeons.Weapons
import org.junit.jupiter.api.Test


class WeaponTest {

    @Test
    fun `a weapon deals proper damage`() {
        val longsword = Weapons.LONGSWORD
        val stats = SOME_STAT_BOCK.copyByInts(str = 16)

        withFixedDice(D8 rolls 6) {
            val damage = longsword.dealDamage({ query -> query(stats) }, false)

            assertThat(damage, equalTo(6 + 3)) // 3 is the modifier for str 16
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

            assertThat(damage, equalTo(4 + 7 + 3)) // 3 is the modifier for str 16
        }

    }
}

class RangeCheckersTest {

    @Test
    fun `melee range checker works correctly`() {
        val meleeChecker = io.dungeons.RangeCheckers.melee(Feet(2.0))

        assertThat(meleeChecker(Feet(1.5)),equalTo(RangeClassification.WithinNormalRange))
        assertThat(meleeChecker(Feet(2.0)),equalTo(RangeClassification.WithinNormalRange))
        assertThat(meleeChecker(Feet(2.5)),equalTo(RangeClassification.OutOfRange))
    }}