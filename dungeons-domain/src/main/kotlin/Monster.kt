package io.dungeons

open class Monster(
    id: Id<CoreEntity> = Id.generate(),
    core: CoreEntityData,
    override val weapon: Weapon,
    override val attackModifier: Int,
) : CoreEntity(
    id,
    core
)

class Goblin(
    id: Id<CoreEntity> = Id.generate(),
    core: CoreEntityData = CoreEntityData(
        name = "Goblin",
        stats = StatBlock(
            str = Stat(8),
            dex = Stat(14),
            con = Stat(10),
            int = Stat(10),
            wis = Stat(8),
            cha = Stat(8),
        ),
        hitPoints = 7,
        armourClass = 15,
        damageModifiers = DamageModifiers.NONE,
    ),
    override val weapon: Weapon = Weapons.Shortbow,
    override val attackModifier: Int = weapon.whichStat(core.stats).modifier,

) : Monster(id, core, weapon, attackModifier)

class KoboldWarrior(
    id: Id<CoreEntity> = Id.generate(),
    core: CoreEntityData = CoreEntityData(
        name = "Kobold Warrior",
        stats = StatBlock(
            str = Stat(7),
            dex = Stat(15),
            con = Stat(9),
            int = Stat(8),
            wis = Stat(7),
            cha = Stat(8),
        ),
        hitPoints = 7,
        armourClass = 14,
        damageModifiers = DamageModifiers.NONE,
    ),
    override val weapon: Weapon = Weapons.Dagger,
    override val attackModifier: Int = weapon.whichStat(core.stats).modifier,
) : Monster(id, core, weapon, attackModifier)