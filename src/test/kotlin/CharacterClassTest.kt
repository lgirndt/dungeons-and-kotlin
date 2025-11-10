import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.example.*
import org.example.Die.Companion.D20
import org.example.WeaponCategory.Martial
import org.example.WeaponCategory.Simple
import org.junit.jupiter.api.Test


class CharacterClassTest {

    private fun aWeaponBeing(category: WeaponCategory) : Weapon = SOME_WEAPON.copy(category = category)

    @Test
    fun `Fighter should be proficient with all weapons`() {
        val fighter = Fighter()
        assertThat(fighter.isProficientWith(aWeaponBeing(Simple)), equalTo(true))
        assertThat(fighter.isProficientWith(aWeaponBeing(Martial)), equalTo(true))
    }

    @Test
    fun `Cleric should be proficient with simple weapons only`() {
        val cleric = Cleric()
        assertThat(cleric.isProficientWith(aWeaponBeing(Simple)), equalTo(true))
        assertThat(cleric.isProficientWith(aWeaponBeing(Martial)), equalTo(false))
    }

    @Test
    fun `Druid should be proficient with simple weapons only`() {
        val druid = Druid()
        assertThat(druid.isProficientWith(aWeaponBeing(Simple)), equalTo(true))
        assertThat(druid.isProficientWith(aWeaponBeing(Martial)), equalTo(false))
    }

    @Test
    fun `Bard should be proficient with simple weapons only`() {
        val bard = Bard()
        assertThat(bard.isProficientWith(aWeaponBeing(Simple)), equalTo(true))
        assertThat(bard.isProficientWith(aWeaponBeing(Martial)), equalTo(false))
    }

    @Test
    fun `A paladin crits only on 20`() {
        val paladin = Paladin()
        assertThat(paladin.isCriticalHit(DieRoll(D20, 20)), equalTo(true))
        assertThat(paladin.isCriticalHit(DieRoll(D20, 19)), equalTo(false))
    }

    @Test
    fun `A fighter crits on 19 and 20`() {
        val fighter = Fighter()
        assertThat(fighter.isCriticalHit(DieRoll(D20, 20)), equalTo(true))
        assertThat(fighter.isCriticalHit(DieRoll(D20, 19)), equalTo(true))
        assertThat(fighter.isCriticalHit(DieRoll(D20, 18)), equalTo(false))
    }

}
