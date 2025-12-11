package domain

import io.dungeons.domain.AttackOutcome
import io.dungeons.domain.Attackable
import io.dungeons.domain.Cleric
import io.dungeons.domain.Creature
import io.dungeons.domain.DamageType
import io.dungeons.domain.Die.Companion.D10
import io.dungeons.domain.Die.Companion.D20
import io.dungeons.domain.Die.Companion.D8
import io.dungeons.domain.RangeCheckers
import io.dungeons.domain.RollModifier
import io.dungeons.domain.SOME_DAMAGE_MODIFIERS
import io.dungeons.domain.SOME_WEAPON
import io.dungeons.domain.SimpleDamageRoll
import io.dungeons.domain.StatBlock
import io.dungeons.domain.StatQueries
import io.dungeons.domain.WeaponCategory.Martial
import io.dungeons.domain.board.BoardPosition
import io.dungeons.domain.combat.ProvidesBoardPosition
import io.dungeons.domain.core.Id
import io.dungeons.domain.world.Feet
import io.dungeons.domain.world.Square
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProvidesBoardPositionMock(val positions: Map<Id<Creature>, BoardPosition>) : ProvidesBoardPosition {
    override fun getBoardPosition(creatureId: Id<Creature>): BoardPosition? = positions[creatureId]
}

class AttackTest {
    lateinit var providesBoardPosition: ProvidesBoardPosition

    val id = TestId<Creature>()

    @BeforeEach
    fun beforeEach() {
        providesBoardPosition = mockk()
    }

    private fun expectAnyGridPosition() {
        every { providesBoardPosition.getBoardPosition(any()) } returns BoardPosition(Square(0), Square(0))
    }

// TODO this will not work aynmore
//    @Test
//    fun `an attacker without a weapon cannot not attack`() {
//        val attacker = SOME_OTHER_CHARACTER.copy()
//        val opponent = SOME_CHARACTER.copy(hitPoints = 20)
//
//        val outcome = attacker.attack(opponent)
//
//        assertEquals(20, opponent.hitPoints)
//        assertEquals(AttackOutcome.MISS, outcome)
//    }

    @Test
    fun `an attacker who does not meet AC misses the attack`() {
        val target = aCharacterWithWeapon(strMod = 1)
        val opponent = aPlayerCharacter(
            armourClass = 13,
        )
        expectAnyGridPosition()

        withFixedDice(D20 rolls 10) {
            val outcome = target.attack(opponent, providesBoardPosition)

            assertEquals(false, outcome.hasBeenHit)
            assertEquals(
                10 + 1 + 1,
                outcome.hitRoll,
                "Hit Roll misses as d20 + str mod + prof bonus does not match AC",
            )
        }
    }

    @Test
    fun `an attacker who meets AC hits the attack`() {
        val damageDie = D8
        val attacker = aCharacterWithWeapon(strMod = 1, damageDie = damageDie)
        val opponent = aPlayerCharacter(armourClass = 10 + 1 + 1)

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 10,
            damageDie rolls 5,
        ) {
            val outcome = attacker.attack(opponent, providesBoardPosition)

            assertEquals(true, outcome.hasBeenHit)
            assertEquals(
                10 + 1 + 1,
                outcome.hitRoll,
                "Hit Roll hits as d20 + str mod + prof bonus  matches AC",
            )
        }
    }

    @Test
    fun `an attack not being proficient should hit without the proficiency bonus applied`() {
        val damageDie = D8
        val martialWeapon = SOME_WEAPON.copy(
            category = Martial,
            damageRoll = SimpleDamageRoll(1, damageDie),
        )
        val cleric = aPlayerCharacter(
            classFeatures = Cleric(),
            stats = StatBlock.fromModifiers(strMod = 2),
            weapon = martialWeapon,
        )

        val opponent = aPlayerCharacter(armourClass = 12)

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 10,
            damageDie rolls 5,
        ) {
            val outcome = cleric.attack(opponent, providesBoardPosition)

            assertEquals(true, outcome.hasBeenHit)
            assertEquals(
                10 + 2,
                outcome.hitRoll, // d20 + str mod only, no proficiency bonus
                "Hit roll without proficiency bonus: d20 + str mod (no prof bonus)",
            )
            assertEquals(5 + 2, outcome.damageDealt) // damage roll + str mod
        }
    }

    private inline fun hitting(
        attackerStrMod: Int,
        opponentHitPoints: Int = 20,
        hitRoll: Int = 10,
        damageRolls: List<Int>,
        damageType: DamageType = DamageType.Slashing,
        opponentVulnerableTo: DamageType? = null,
        runTest: (outcome: AttackOutcome, opponent: Attackable) -> Unit,
    ) {
        val damageDie = D10
        val attacker = aCharacterWithWeapon(
            strMod = attackerStrMod,
            damageType = damageType,
            damageDie = damageDie,
        )
        val opponent = aPlayerCharacter(
            hitPoints = opponentHitPoints,
            armourClass = 10,
            damageModifiers = SOME_DAMAGE_MODIFIERS.copy(
                vulnerabilities = if (opponentVulnerableTo != null) setOf(opponentVulnerableTo) else emptySet(),
            ),
        )

        val diceRolls = listOf(D20 rolls hitRoll) + damageRolls.map { damageDie rolls it }

        expectAnyGridPosition()

        withFixedDice(*diceRolls.toTypedArray()) {
            val outcome = attacker.attack(opponent, providesBoardPosition)
            runTest(outcome, opponent)
        }
    }

    @Test
    fun `a hit does normal damage`() {
        hitting(attackerStrMod = 1, damageRolls = listOf(5)) { outcome, _ ->
            assertEquals(5 + 1, outcome.damageDealt)
        }
    }

    @Test
    fun `a critical hit does double damage dice`() {
        hitting(
            attackerStrMod = 1,
            hitRoll = 20,
            damageRolls = listOf(5, 8),
        ) { outcome, _ ->
            assertEquals(5 + 8 + 1, outcome.damageDealt)
        }
    }

    @Test
    fun `an opponent receives damage properly`() {
        hitting(
            attackerStrMod = 2,
            opponentHitPoints = 30,
            damageRolls = listOf(6),
        ) { _, opponent ->
            assertEquals(30 - 6 - 2, opponent.hitPoints)
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
            opponentVulnerableTo = DamageType.Slashing,
        ) { _, opponent ->
            assertEquals(0, opponent.hitPoints)
        }
    }

    @Test
    fun `attacking with NORMAL modifier uses single die roll`() {
        val damageDie = D8
        val attacker = aCharacterWithWeapon(strMod = 2, damageDie = damageDie)
        val opponent = aPlayerCharacter(armourClass = 10)

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 10,
            damageDie rolls 6,
        ) {
            val outcome = attacker.attack(opponent, providesBoardPosition, RollModifier.NORMAL)

            assertEquals(true, outcome.hasBeenHit)
            assertEquals(
                10 + 2 + 1,
                outcome.hitRoll, // d20 + str mod + prof bonus
                "Hit roll uses single d20 roll",
            )
            assertEquals(6 + 2, outcome.damageDealt) // damage roll + str mod
        }
    }

    @Test
    fun `attacking with ADVANTAGE uses higher of two rolls to hit`() {
        val damageDie = D8
        val attacker = aCharacterWithWeapon(strMod = 2, damageDie = damageDie)
        val opponent = aPlayerCharacter(armourClass = 10)

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 8,
            D20 rolls 15, // higher roll should be used
            damageDie rolls 6,
        ) {
            val outcome = attacker.attack(opponent, providesBoardPosition, RollModifier.ADVANTAGE)

            assertEquals(true, outcome.hasBeenHit)
            assertEquals(
                15 + 2 + 1,
                outcome.hitRoll, // max(8, 15) + str mod + prof bonus
                "Hit roll uses higher of two d20 rolls",
            )
            assertEquals(6 + 2, outcome.damageDealt)
        }
    }

    @Test
    fun `attacking with DISADVANTAGE uses lower of two rolls to hit`() {
        val damageDie = D8
        val attacker = aCharacterWithWeapon(strMod = 2, damageDie = damageDie)
        val opponent = aPlayerCharacter(armourClass = 10)

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 15,
            D20 rolls 8, // lower roll should be used
            damageDie rolls 6,
        ) {
            val outcome = attacker.attack(opponent, providesBoardPosition, RollModifier.DISADVANTAGE)

            assertEquals(true, outcome.hasBeenHit)
            assertEquals(
                8 + 2 + 1,
                outcome.hitRoll, // min(15, 8) + str mod + prof bonus
                "Hit roll uses lower of two d20 rolls",
            )
            assertEquals(6 + 2, outcome.damageDealt)
        }
    }

    @Test
    fun `attacking with ADVANTAGE can turn a miss into a hit`() {
        val attacker = aCharacterWithWeapon(strMod = 1)
        val opponent = aPlayerCharacter(armourClass = 15)

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 5, // would miss: 5 + 1 + 1 = 7 < 15
            D20 rolls 13, // hits: 13 + 1 + 1 = 15
            D8 rolls 4,
        ) {
            val outcome = attacker.attack(opponent, providesBoardPosition, RollModifier.ADVANTAGE)

            assertEquals(true, outcome.hasBeenHit)
            assertEquals(13 + 1 + 1, outcome.hitRoll)
        }
    }

    @Test
    fun `attacking with DISADVANTAGE can turn a hit into a miss`() {
        val attacker = aCharacterWithWeapon(strMod = 1)
        val opponent = aPlayerCharacter(armourClass = 15)

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 13, // would hit: 13 + 1 + 1 = 15
            D20 rolls 5, // misses: 5 + 1 + 1 = 7 < 15
        ) {
            val outcome = attacker.attack(opponent, providesBoardPosition, RollModifier.DISADVANTAGE)

            assertEquals(false, outcome.hasBeenHit)
            assertEquals(5 + 1 + 1, outcome.hitRoll)
        }
    }

    @Test
    fun `critical hit works with ADVANTAGE when either roll is 20`() {
        val damageDie = D8
        val attacker = aCharacterWithWeapon(strMod = 2, damageDie = damageDie)
        val opponent = aPlayerCharacter(armourClass = 10)

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 12,
            D20 rolls 20, // critical hit
            damageDie rolls 5,
            damageDie rolls 7, // second damage roll for crit
        ) {
            val outcome = attacker.attack(opponent, providesBoardPosition, RollModifier.ADVANTAGE)

            assertEquals(true, outcome.hasBeenHit)
            assertEquals(5 + 7 + 2, outcome.damageDealt) // double dice + str mod
        }
    }

    @Test
    fun `critical hit works with DISADVANTAGE when higher roll is 20`() {
        val damageDie = D8
        val attacker = aCharacterWithWeapon(strMod = 2, damageDie = damageDie)
        val opponent = aPlayerCharacter(armourClass = 10)

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 20, // this is the max, so it's used even with disadvantage
            D20 rolls 20, // both are 20, so critical hit triggers
            damageDie rolls 5,
            damageDie rolls 7, // second damage roll for crit
        ) {
            val outcome = attacker.attack(opponent, providesBoardPosition, RollModifier.DISADVANTAGE)

            assertEquals(true, outcome.hasBeenHit)
            assertEquals(5 + 7 + 2, outcome.damageDealt) // double dice + str mod
        }
    }

    @Test
    fun `a opponent within melee range can be attacked`() {
        val damageDie = D8
        val attacker = aPlayerCharacter(
            weapon = SOME_WEAPON.copy(
                rangeChecker = RangeCheckers.melee(Feet(5.0)),
            ),
        )

        val opponent = aPlayerCharacter(
            armourClass = 10,
        )

        expectAnyGridPosition()

        withFixedDice(
            D20 rolls 10,
            damageDie rolls 6,
        ) {
            val outcome = attacker.attack(opponent, providesBoardPosition)
            assertEquals(true, outcome.hasBeenHit)
        }
    }

    @Test
    fun `an attack out of melee range misses automatically`() {
        val attacker = aPlayerCharacter(
            id = id[0],
            weapon = SOME_WEAPON.copy(
                rangeChecker = RangeCheckers.melee(Feet(5.0)),
            ),
        )

        val opponent = aPlayerCharacter(
            id = id[1],
            armourClass = 10,
        )

        providesBoardPosition = ProvidesBoardPositionMock(
            mapOf(
                id[0] to BoardPosition(Square(0), Square(0)),
                id[1] to BoardPosition(Square(2), Square(0)),
            ),
        )

        withFixedDice {
            val outcome = attacker.attack(opponent, providesBoardPosition)
            assertEquals(AttackOutcome.MISS, outcome)
        }
    }

    @Test
    fun `a ranged weapon attack within normal range hits normally`() {
        providesBoardPosition = ProvidesBoardPositionMock(
            mapOf(
                id[0] to BoardPosition(Square(0), Square(0)),
                id[1] to BoardPosition(Square(1), Square(0)),
            ),
        )

        val damageDie = D8
        val attacker = aPlayerCharacter(
            id = id[0],
            weapon = SOME_WEAPON.copy(
                name = "Test Weapon",
                statQuery = StatQueries.Dex,
                rangeChecker = RangeCheckers.ranged(normalRange = Feet(10.0), longRange = Feet(30.0)),
            ),
        )

        val opponent = aPlayerCharacter(
            id = id[1],
            armourClass = 10,
        )

        withFixedDice(
            D20 rolls 10,
            damageDie rolls 6,
        ) {
            val outcome = attacker.attack(opponent, providesBoardPosition)
            assertEquals(true, outcome.hasBeenHit)
        }
    }

    @Test
    fun `a ranged weapon attack within long range hits with disadvantage`() {
        val damageDie = D8
        val attacker = aPlayerCharacter(
            id = id[0],
            weapon = SOME_WEAPON.copy(
                statQuery = StatQueries.Dex,
                rangeChecker = RangeCheckers.ranged(normalRange = Feet(10.0), longRange = Feet(30.0)),
            ),
        )

        val opponent = aPlayerCharacter(
            id = id[1],
            armourClass = 10,
        )

        providesBoardPosition = ProvidesBoardPositionMock(
            mapOf(
                id[0] to BoardPosition(Square(0), Square(0)),
                id[1] to BoardPosition(Square(5), Square(0)),
            ),
        )

        withFixedDice(
            D20 rolls 10, // lower roll should be used
            D20 rolls 15,
            damageDie rolls 6,
        ) {
            val outcome = attacker.attack(opponent, providesBoardPosition)
            assertEquals(true, outcome.hasBeenHit)
            assertEquals(10 + 1, outcome.hitRoll)
        }
    }
}
