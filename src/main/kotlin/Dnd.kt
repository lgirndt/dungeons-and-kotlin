package org.example


data  class Stat(val value: UInt) {
    val modifier : Int
        get() = value.toInt() / 2 - 5
}

data class StatBlock(
    val str : Stat,
    val dex : Stat,
    val con : Stat,
    val int : Stat,
    val wis : Stat,
    val cha : Stat,
) {
    constructor(
        str: UInt,
        dex: UInt,
        con: UInt,
        int: UInt,
        wis: UInt,
        cha: UInt)
            : this(
        Stat(str),
        Stat(dex),
        Stat(con),
        Stat(int),
        Stat(wis),
        Stat(cha),
    )

    companion object {}
}

sealed class CharacterClass(
    val hitDie: Die
)  {
   val name: String
        get() = this.javaClass.simpleName

    object Fighter: CharacterClass(Die.D10)
    object Cleric: CharacterClass(Die.D8)
    object Druid: CharacterClass(Die.D8)
    object Barbarian: CharacterClass(Die.D12)
    object Paladin: CharacterClass(Die.D10)
    object Ranger: CharacterClass(Die.D10)
    object Rogue: CharacterClass(Die.D8)
    object Warlock: CharacterClass(Die.D8)
    object Monk: CharacterClass(Die.D8)
    object Sorcerer: CharacterClass(Die.D6)
    object Bard: CharacterClass(Die.D8)
    object Wizard: CharacterClass(Die.D8)
}

data class DamageModifiers(
    val resistances: Set<DamageType> = emptySet(),
    val immunities: Set<DamageType> = emptySet(),
    val vulnerabilities: Set<DamageType> = emptySet(),
) {
    companion object {
        val NONE = DamageModifiers()
    }
}

data class Character(
    val name: String,
    val characterClass: CharacterClass,
    val stats : StatBlock,
    val level: Int = 1,
    val damageModifiers: DamageModifiers = DamageModifiers.NONE,
) {
    companion object
}

data class Die(val numberOfFaces: Int) {
    companion object {
        val D6 = Die(6)
        val D8 = Die(8)
        val D10 = Die(10)
        val D12 = Die(12)
    }
}

enum class DamageType {
    Slashing,
    Piercing,
    Bludgeoning,
    Fire,
    Cold,
    Lightning,
    Acid,
    Poison,
    Psychic,
    Necrotic,
    Radiant,
    Thunder,
    Force,
}
