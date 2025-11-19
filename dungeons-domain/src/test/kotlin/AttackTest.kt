
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import io.dungeons.*
import io.dungeons.Die.Companion.D10
import io.dungeons.Die.Companion.D20
import io.dungeons.Die.Companion.D8
import io.dungeons.WeaponCategory.Martial
import io.dungeons.combat.ProvidesGridPosition
import io.dungeons.core.Id
import io.dungeons.world.Feet
import io.dungeons.world.GridPosition
import io.dungeons.world.Square
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class ProvidesGridPositionMock(
    val positions: Map<Id<CoreEntity>, GridPosition>
) : ProvidesGridPosition  {
    override fun getGridPosition(entityId: Id<CoreEntity>): GridPosition? {
        return positions[entityId]
    }

}

class AttackTest {

    lateinit var providesGridPosition: ProvidesGridPosition
    val ID = TestId<CoreEntity>()

    @BeforeEach
    fun beforeEach() {
        providesGridPosition = mockk()
    }

    private fun expectAnyGridPosition() {
        every { providesGridPosition.getGridPosition(any()) } returns GridPosition(Square(0), Square(0))
    }

// TODO this will not work aynmore
//    @Test
//    fun `an attacker without a weapon cannot not attack`() {
//        val attacker = SOME_OTHER_CHARACTER.copy()
//        val opponent = SOME_CHARACTER.copy(hitPoints = 20)
//
//        val outcome = attacker.attack(opponent)
//
//        assertThat(opponent.hitPoints, equalTo(20))
//        assertThat(outcome, equalTo(AttackOutcome.MISS))
//    }

    @Test
    fun `an attacker who does not meet AC misses the attack`() {
        val target = aCharacterWithWeapon(strMod = 1)
        val opponent = PlayerCharacter.aPlayerCharacter(
            armourClass = 13
        )
        expectAnyGridPosition()

        withFixedDice(D20 rolls 10) {
            val outcome = target.attack(opponent, providesGridPosition)

            assertThat(outcome.hasBeenHit, equalTo(false))
            assertThat(
                "Hit Roll misses as d20 + str mod + prof bonus does not match AC",
                outcome.hitRoll, equalTo(10 + 1 + 1)
            )
        }

    }

    @Test
    fun `an attacker who meets AC hits the attack`() {
        val damageDie = D8
        val attacker = aCharacterWithWeapon(strMod = 1, damageDie = damageDie)
        val opponent = PlayerCharacter.aPlayerCharacter(armourClass = 10 + 1 + 1)

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 10,
            damageDie rolls 5
        ) {
            val outcome = attacker.attack(opponent, providesGridPosition)

            assertThat(outcome.hasBeenHit, equalTo(true))
            assertThat(
                "Hit Roll hits as d20 + str mod + prof bonus  matches AC",
                outcome.hitRoll, equalTo(10 + 1 + 1)
            )
        }

    }

    @Test
    fun `an attack not being proficient should hit without the proficiency bonus applied`() {
        val damageDie = D8
        val martialWeapon = SOME_WEAPON.copy(
            category = Martial,
            damageRoll = SimpleDamageRoll(1, damageDie)
        )
        val cleric = PlayerCharacter.aPlayerCharacter(
            classFeatures = Cleric(),
            stats = StatBlock.fromModifiers(strMod = 2),
            weapon = martialWeapon
        )

        val opponent = PlayerCharacter.aPlayerCharacter(armourClass = 12)

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 10,
            damageDie rolls 5
        ) {
            val outcome = cleric.attack(opponent, providesGridPosition)

            assertThat(outcome.hasBeenHit, equalTo(true))
            assertThat(
                "Hit roll without proficiency bonus: d20 + str mod (no prof bonus)",
                outcome.hitRoll, equalTo(10 + 2) // d20 + str mod only, no proficiency bonus
            )
            assertThat(outcome.damageDealt, equalTo(5 + 2)) // damage roll + str mod
        }
    }

    private inline fun hitting(
        attackerStrMod: Int,
        opponentHitPoints: Int = 20,
        hitRoll: Int = 10,
        damageRolls: List<Int>,
        damageType: DamageType = DamageType.Slashing,
        opponentVulnerableTo: DamageType? = null,
        runTest: (outcome: AttackOutcome, opponent: Attackable) -> Unit
    ) {
        val damageDie = D10
        val attacker = aCharacterWithWeapon(
            strMod = attackerStrMod,
            damageType = damageType,
            damageDie = damageDie
        )
        val opponent = PlayerCharacter.aPlayerCharacter(
            hitPoints = opponentHitPoints,
            armourClass = 10,
            damageModifiers = SOME_DAMAGE_MODIFIERS.copy(
                vulnerabilities = if (opponentVulnerableTo != null) setOf(opponentVulnerableTo) else emptySet()
            )
        )

        val diceRolls = listOf(D20 rolls hitRoll) + damageRolls.map { damageDie rolls it }

        expectAnyGridPosition()

        withFixedDice(*diceRolls.toTypedArray()) {
            val outcome = attacker.attack(opponent, providesGridPosition)
            runTest(outcome, opponent)
        }

    }

    @Test
    fun `a hit does normal damage`() {
        hitting(attackerStrMod = 1, damageRolls = listOf(5)) { outcome, _ ->
            assertThat(outcome.damageDealt, equalTo(5 + 1))
        }
    }

    @Test
    fun `a critical hit does double damage dice`() {
        hitting(
            attackerStrMod = 1,
            hitRoll = 20,
            damageRolls = listOf(5, 8)
        ) { outcome, _ ->
            assertThat(outcome.damageDealt, equalTo(5 + 8 + 1))
        }
    }

    @Test
    fun `an opponent receives damage properly`() {
        hitting(
            attackerStrMod = 2,
            opponentHitPoints = 30,
            damageRolls = listOf(6)
        ) { _, opponent ->
            assertThat(opponent.hitPoints, equalTo(30 - 6 - 2))
        }
    }

    @Test
    fun `one hitting with a crit on vulnerability`() {
        hitting(
            attackerStrMod = 2,
            opponentHitPoints = 38,
            hitRoll = 20,
            damageRolls = listOf(8, 9),
            damageType = DamageType.Slashing,
            opponentVulnerableTo = DamageType.Slashing
        ) { _, opponent ->
            assertThat(opponent.hitPoints, equalTo(0))
        }
    }

    @Test
    fun `attacking with NORMAL modifier uses single die roll`() {
        val damageDie = D8
        val attacker = aCharacterWithWeapon(strMod = 2, damageDie = damageDie)
        val opponent = PlayerCharacter.aPlayerCharacter(armourClass = 10)

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 10,
            damageDie rolls 6
        ) {
            val outcome = attacker.attack(opponent, providesGridPosition, RollModifier.NORMAL)

            assertThat(outcome.hasBeenHit, equalTo(true))
            assertThat(
                "Hit roll uses single d20 roll",
                outcome.hitRoll, equalTo(10 + 2 + 1) // d20 + str mod + prof bonus
            )
            assertThat(outcome.damageDealt, equalTo(6 + 2)) // damage roll + str mod
        }
    }

    @Test
    fun `attacking with ADVANTAGE uses higher of two rolls to hit`() {
        val damageDie = D8
        val attacker = aCharacterWithWeapon(strMod = 2, damageDie = damageDie)
        val opponent = PlayerCharacter.aPlayerCharacter(armourClass = 10)

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 8,
            D20 rolls 15,  // higher roll should be used
            damageDie rolls 6
        ) {
            val outcome = attacker.attack(opponent, providesGridPosition, RollModifier.ADVANTAGE)

            assertThat(outcome.hasBeenHit, equalTo(true))
            assertThat(
                "Hit roll uses higher of two d20 rolls",
                outcome.hitRoll, equalTo(15 + 2 + 1) // max(8, 15) + str mod + prof bonus
            )
            assertThat(outcome.damageDealt, equalTo(6 + 2))
        }
    }

    @Test
    fun `attacking with DISADVANTAGE uses lower of two rolls to hit`() {
        val damageDie = D8
        val attacker = aCharacterWithWeapon(strMod = 2, damageDie = damageDie)
        val opponent = PlayerCharacter.aPlayerCharacter(armourClass = 10)

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 15,
            D20 rolls 8,  // lower roll should be used
            damageDie rolls 6
        ) {
            val outcome = attacker.attack(opponent, providesGridPosition, RollModifier.DISADVANTAGE)

            assertThat(outcome.hasBeenHit, equalTo(true))
            assertThat(
                "Hit roll uses lower of two d20 rolls",
                outcome.hitRoll, equalTo(8 + 2 + 1) // min(15, 8) + str mod + prof bonus
            )
            assertThat(outcome.damageDealt, equalTo(6 + 2))
        }
    }

    @Test
    fun `attacking with ADVANTAGE can turn a miss into a hit`() {
        val attacker = aCharacterWithWeapon(strMod = 1)
        val opponent = PlayerCharacter.aPlayerCharacter(armourClass = 15)

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 5,  // would miss: 5 + 1 + 1 = 7 < 15
            D20 rolls 13, // hits: 13 + 1 + 1 = 15
            D8 rolls 4
        ) {
            val outcome = attacker.attack(opponent, providesGridPosition, RollModifier.ADVANTAGE)

            assertThat(outcome.hasBeenHit, equalTo(true))
            assertThat(outcome.hitRoll, equalTo(13 + 1 + 1))
        }
    }

    @Test
    fun `attacking with DISADVANTAGE can turn a hit into a miss`() {
        val attacker = aCharacterWithWeapon(strMod = 1)
        val opponent = PlayerCharacter.aPlayerCharacter(armourClass = 15)

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 13, // would hit: 13 + 1 + 1 = 15
            D20 rolls 5   // misses: 5 + 1 + 1 = 7 < 15
        ) {
            val outcome = attacker.attack(opponent, providesGridPosition, RollModifier.DISADVANTAGE)

            assertThat(outcome.hasBeenHit, equalTo(false))
            assertThat(outcome.hitRoll, equalTo(5 + 1 + 1))
        }
    }

    @Test
    fun `critical hit works with ADVANTAGE when either roll is 20`() {
        val damageDie = D8
        val attacker = aCharacterWithWeapon(strMod = 2, damageDie = damageDie)
        val opponent = PlayerCharacter.aPlayerCharacter(armourClass = 10)

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 12,
            D20 rolls 20,  // critical hit
            damageDie rolls 5,
            damageDie rolls 7   // second damage roll for crit
        ) {
            val outcome = attacker.attack(opponent, providesGridPosition, RollModifier.ADVANTAGE)

            assertThat(outcome.hasBeenHit, equalTo(true))
            assertThat(outcome.damageDealt, equalTo(5 + 7 + 2)) // double dice + str mod
        }
    }

    @Test
    fun `critical hit works with DISADVANTAGE when higher roll is 20`() {
        val damageDie = D8
        val attacker = aCharacterWithWeapon(strMod = 2, damageDie = damageDie)
        val opponent = PlayerCharacter.aPlayerCharacter(armourClass = 10)

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 20,  // this is the max, so it's used even with disadvantage
            D20 rolls 20,  // both are 20, so critical hit triggers
            damageDie rolls 5,
            damageDie rolls 7   // second damage roll for crit
        ) {
            val outcome = attacker.attack(opponent, providesGridPosition, RollModifier.DISADVANTAGE)

            assertThat(outcome.hasBeenHit, equalTo(true))
            assertThat(outcome.damageDealt, equalTo(5 + 7 + 2)) // double dice + str mod
        }
    }

    @Test
    fun `a opponent within melee range can be attacked`() {
        val damageDie = D8
        val attacker = PlayerCharacter.aPlayerCharacter(

            weapon = SOME_WEAPON.copy(
                rangeChecker = RangeCheckers.melee(Feet(5.0))
            )
        )

        val opponent = PlayerCharacter.aPlayerCharacter(
            armourClass = 10,
        )

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 10,
            damageDie rolls 6
        ) {
            val outcome = attacker.attack(opponent, providesGridPosition)
            assertThat(outcome.hasBeenHit, equalTo(true))
        }
    }

    @Test
    fun `an attack out of melee range misses automatically`() {
        val attacker = PlayerCharacter.aPlayerCharacter(
            id = ID[0],
            weapon = SOME_WEAPON.copy(
                rangeChecker = RangeCheckers.melee(Feet(5.0))
            )
        )

        val opponent = PlayerCharacter.aPlayerCharacter(
            id = ID[1],
            armourClass = 10,
        )

        providesGridPosition = ProvidesGridPositionMock(
            mapOf(
                ID[0] to GridPosition(Square(0), Square(0)),
                ID[1] to GridPosition(Square(2), Square(0))
            )
        )

        withFixedDice {
            val outcome = attacker.attack(opponent, providesGridPosition)
            assertThat(outcome, equalTo(AttackOutcome.MISS))
        }
    }

    @Test
    fun `a ranged weapon attack within normal range hits normally`() {

        providesGridPosition = ProvidesGridPositionMock(
            mapOf(
                ID[0] to GridPosition(Square(0), Square(0)),
                ID[1] to GridPosition(Square(1), Square(0))
            )
        )

        val damageDie = D8
        val attacker = PlayerCharacter.aPlayerCharacter(
            id = ID[0],
            weapon = SOME_WEAPON.copy(
                name = "Test Weapon",
                statQuery = StatQueries.Dex,
                rangeChecker = RangeCheckers.ranged(normalRange = Feet(10.0), longRange = Feet(30.0))
            )
        )

        val opponent = PlayerCharacter.aPlayerCharacter(
            id = ID[1],
            armourClass = 10,
        )


        withFixedDice(
            D20 rolls 10,
            damageDie rolls 6
        ) {
            val outcome = attacker.attack(opponent, providesGridPosition)
            assertThat(outcome.hasBeenHit, equalTo(true))
        }
    }

    @Test
    fun `a ranged weapon attack within long range hits with disadvantage`() {
        val damageDie = D8
        val attacker = PlayerCharacter.aPlayerCharacter(
            id = ID[0],
            weapon = SOME_WEAPON.copy(
                statQuery = StatQueries.Dex,
                rangeChecker = RangeCheckers.ranged(normalRange = Feet(10.0), longRange = Feet(30.0))
            )
        )

        val opponent = PlayerCharacter.aPlayerCharacter(
            id = ID[1],
            armourClass = 10,
        )

        providesGridPosition = ProvidesGridPositionMock(
            mapOf(
                ID[0] to GridPosition(Square(0), Square(0)),
                ID[1] to GridPosition(Square(5), Square(0))
            )
        )

        withFixedDice(
            D20 rolls 10, // lower roll should be used
            D20 rolls 15,
            damageDie rolls 6
        ) {
            val outcome = attacker.attack(opponent, providesGridPosition)
            assertThat(outcome.hasBeenHit, equalTo(true))
            assertThat(outcome.hitRoll, equalTo(10 + 1))
        }
    }

}
