package org.example

data class Monster(
    val name: String,
    val stats: StatBlock,
    var attackSource: AttackSource,
    override val armourClass: Int,
    override var position: Coordinate,
    override val damageModifiers: DamageModifiers,
    override var hitPoints: Int

) : Attackable

object Bestiary {
    fun Goblin(): Monster {
        return Monster(
            name = "Goblin",
            stats = StatBlock(
                str = Stat(8),
                dex = Stat(14),
                con = Stat(10),
                int = Stat(10),
                wis = Stat(8),
                cha = Stat(8),
            ),
            attackSource = Weapons.Shortbow,
            armourClass = 15,
            position = Coordinate.from(0, 0),
            damageModifiers = DamageModifiers.NONE,
            hitPoints = 7,
        )
    }
}