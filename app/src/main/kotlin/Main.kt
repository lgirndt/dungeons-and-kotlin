
import io.dungeons.*
import io.dungeons.board.BoardPosition
import io.dungeons.combat.ProvidesGridPosition
import io.dungeons.core.Id
import io.dungeons.world.Square

fun main() {
    val fighter = PlayerCharacter(
        data = PlayerCharacterData(
            level = 5,
            weapon = Weapons.LONGSWORD
        ),
        classFeatures = Fighter(),
        core = CoreEntityData(
            name = "Aragorn",
            stats = StatBlock.create(
                str = 16,
                dex = 13,
                con = 14,
                int = 10,
                wis = 12,
                cha = 14
            ),
            hitPoints = 15,
            armourClass = 13,
            damageModifiers = DamageModifiers.NONE,
        )
    )

    println("Hello, ${fighter.name}! You've reached Nowhere.")

    val goblin = Goblin()
    println("A wild ${goblin.name} appears!")

    println("${fighter.name} attacks the ${goblin.name} with a ${fighter.weapon.name}.")
    val providesGridPosition : ProvidesGridPosition= object : ProvidesGridPosition {
        override fun getGridPosition(entityId: Id<CoreEntity>): BoardPosition {
            return when (entityId) {
                fighter.id -> BoardPosition(Square(0), Square(0))
                goblin.id -> BoardPosition(Square(0), Square(1))
                else -> throw IllegalArgumentException("Unknown entity ID")
            }
        }
    }
    val outcome = fighter.attack(goblin, providesGridPosition)
    println("${fighter.name} rolled a ${outcome.hitRoll}")
    if (outcome.hasBeenHit) {
        println("${fighter.name} hits the ${goblin.name} for ${outcome.damageDealt} damage!")
    } else {
        println("${fighter.name} misses the ${goblin.name}.")
    }
    when{
        goblin.hitPoints == goblin.maxHitPoints -> println("The ${goblin.name} is unscathed and laughs at you.")
        goblin.hitPoints <= 0 -> println("The ${goblin.name} has been defeated!")
        else -> println("The ${goblin.name} has ${goblin.hitPoints} hit points remaining.")
    }
}