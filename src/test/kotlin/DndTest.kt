import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.example.*
import org.example.CharacterClass.Barbarian
import org.example.CharacterClass.Warlock
import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith


class DndTest {


    @Test
    fun `a modifier should return the correct value`() {
        assertAll(
            { assertThat(Stat(8u).modifier, equalTo(-1)) },
            { assertThat(Stat(9u).modifier, equalTo(-1)) },

            { assertThat(Stat(10u).modifier, equalTo(0)) },
            { assertThat(Stat(11u).modifier, equalTo(0)) },

            { assertThat(Stat(12u).modifier, equalTo(1)) },
            { assertThat(Stat(13u).modifier, equalTo(1)) },
        )
    }

    @Test
    fun `overloaded ctor should assign values correctly`() {
        val statBlocks = StatBlock(10u, 11u, 12u, 13u, 14u, 15u)
        assertAll(
            { assertThat(statBlocks.str.value, equalTo(10u)) },
            { assertThat(statBlocks.dex.value, equalTo(11u)) },
            { assertThat(statBlocks.con.value, equalTo(12u)) },
            { assertThat(statBlocks.int.value, equalTo(13u)) },
            { assertThat(statBlocks.wis.value, equalTo(14u)) },
            { assertThat(statBlocks.cha.value, equalTo(15u)) },
        )
    }

    @Test
    fun `some StatBlock`() {
        val dexBlock = StatBlock.create(dex = 12u)
        assertAll(
            { assertThat(dexBlock.dex.value, equalTo(12u)) },
            { assertThat(dexBlock.str.value, equalTo(DEFAULT_STAT_VALUE)) },
        )
    }

    @Test
    fun `CharacterClasses have the proper name`() {
        assertThat(Barbarian.name, equalTo("Barbarian"))
    }

}

