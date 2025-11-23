import org.junit.jupiter.api.Assertions.assertEquals
import io.dungeons.*
import io.dungeons.Die.Companion.D20
import io.dungeons.WeaponCategory.Martial
import io.dungeons.WeaponCategory.Simple
import org.junit.jupiter.api.Test


class ClassFeaturesTest {

    private fun aWeaponBeing(category: WeaponCategory) : Weapon = SOME_WEAPON.copy(category = category)

    @Test
    fun `Fighter should be proficient with all weapons`() {
        val fighter = Fighter()
        assertEquals(true, fighter.isProficientWith(aWeaponBeing(Simple)))
        assertEquals(true, fighter.isProficientWith(aWeaponBeing(Martial)))
    }

    @Test
    fun `Cleric should be proficient with simple weapons only`() {
        val cleric = Cleric()
        assertEquals(true, cleric.isProficientWith(aWeaponBeing(Simple)))
        assertEquals(false, cleric.isProficientWith(aWeaponBeing(Martial)))
    }

    @Test
    fun `Druid should be proficient with simple weapons only`() {
        val druid = Druid()
        assertEquals(true, druid.isProficientWith(aWeaponBeing(Simple)))
        assertEquals(false, druid.isProficientWith(aWeaponBeing(Martial)))
    }

    @Test
    fun `Bard should be proficient with simple weapons only`() {
        val bard = Bard()
        assertEquals(true, bard.isProficientWith(aWeaponBeing(Simple)))
        assertEquals(false, bard.isProficientWith(aWeaponBeing(Martial)))
    }

    @Test
    fun `A paladin crits only on 20`() {
        val paladin = Paladin()
        assertEquals(true, paladin.isCriticalHit(DieRoll(D20, 20)))
        assertEquals(false, paladin.isCriticalHit(DieRoll(D20, 19)))
    }

    @Test
    fun `A fighter crits on 19 and 20`() {
        val fighter = Fighter()
        assertEquals(true, fighter.isCriticalHit(DieRoll(D20, 20)))
        assertEquals(true, fighter.isCriticalHit(DieRoll(D20, 19)))
        assertEquals(false, fighter.isCriticalHit(DieRoll(D20, 18)))
    }

}
