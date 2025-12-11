package io.dungeons.domain

const val DEFAULT_STAT_VALUE = 10

val SOME_STAT_BOCK = StatBlock(
    str = Stat(DEFAULT_STAT_VALUE),
    dex = Stat(DEFAULT_STAT_VALUE),
    con = Stat(DEFAULT_STAT_VALUE),
    int = Stat(DEFAULT_STAT_VALUE),
    wis = Stat(DEFAULT_STAT_VALUE),
    cha = Stat(DEFAULT_STAT_VALUE),
)

val SOME_WEAPON = Weapon(
    name = "Surgebinder",
    category = WeaponCategory.Martial,
    damageType = DamageType.Slashing,
    statQuery = StatQueries.Str,
    damageRoll = SimpleDamageRoll(1, Die.D8),
    rangeChecker = RangeCheckers.melee(io.dungeons.domain.world.Feet(5.0)),
)

val SOME_DAMAGE_MODIFIERS = DamageModifiers(
    resistances = emptySet(),
    immunities = emptySet(),
    vulnerabilities = emptySet(),
)
