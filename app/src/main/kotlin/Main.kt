
import io.dungeons.*

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

//    val goblin = Goblin()
//    println("A wild ${goblin.name} appears!")
//
//    println("${fighter.name} attacks the ${goblin.name} with a ${fighter.weapon.name}.")
//    val outcome = fighter.attack(goblin)
//    println("${fighter.name} rolled a ${outcome.hitRoll}")
//    if (outcome.hasBeenHit) {
//        println("${fighter.name} hits the ${goblin.name} for ${outcome.damageDealt} damage!")
//    } else {
//        println("${fighter.name} misses the ${goblin.name}.")
//    }
}