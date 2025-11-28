package io.dungeons.app

import io.dungeons.domain.Creature
import io.dungeons.domain.CreatureData
import io.dungeons.domain.DamageModifiers
import io.dungeons.domain.Fighter
import io.dungeons.domain.Goblin
import io.dungeons.domain.PlayerCharacter
import io.dungeons.domain.PlayerCharacterData
import io.dungeons.domain.StatBlock
import io.dungeons.domain.Weapons
import io.dungeons.domain.board.BoardPosition
import io.dungeons.domain.combat.ProvidesBoardPosition
import io.dungeons.domain.core.Id
import io.dungeons.domain.world.Square

fun main() {
    val fighter = PlayerCharacter(
        data = PlayerCharacterData(
            level = 5,
            weapon = Weapons.LONGSWORD,
        ),
        classFeatures = Fighter(),
        core = CreatureData(
            name = "Aragorn",
            stats = StatBlock.create(
                str = 16,
                dex = 13,
                con = 14,
                int = 10,
                wis = 12,
                cha = 14,
            ),
            hitPoints = 15,
            armourClass = 13,
            damageModifiers = DamageModifiers.NONE,
        ),
    )

    println("Hello, ${fighter.name}! You've reached Nowhere.")

    val goblin = Goblin()
    println("A wild ${goblin.name} appears!")

    println("${fighter.name} attacks the ${goblin.name} with a ${fighter.weapon.name}.")
    val providesBoardPosition: ProvidesBoardPosition = object : ProvidesBoardPosition {
        override fun getBoardPosition(creatureId: Id<Creature>): BoardPosition = when (creatureId) {
            fighter.id -> BoardPosition(Square(0), Square(0))
            goblin.id -> BoardPosition(Square(0), Square(1))
            else -> throw IllegalArgumentException("Unknown creature ID")
        }
    }
    val outcome = fighter.attack(goblin, providesBoardPosition)
    println("${fighter.name} rolled a ${outcome.hitRoll}")
    if (outcome.hasBeenHit) {
        println("${fighter.name} hits the ${goblin.name} for ${outcome.damageDealt} damage!")
    } else {
        println("${fighter.name} misses the ${goblin.name}.")
    }
    when {
        goblin.hitPoints == goblin.maxHitPoints -> println("The ${goblin.name} is unscathed and laughs at you.")
        goblin.hitPoints <= 0 -> println("The ${goblin.name} has been defeated!")
        else -> println("The ${goblin.name} has ${goblin.hitPoints} hit points remaining.")
    }
}
