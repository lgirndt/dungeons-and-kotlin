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

//interface CharacterClass {
//    val name: String
//    val hitDie: HitDie
//}

sealed class CharacterClass(
    val hitDie: HitDie
)  {
   val name: String
        get() = this.javaClass.simpleName

    object Fighter: CharacterClass(HitDie.D10)
    object Cleric: CharacterClass(HitDie.D8)
    object Druid: CharacterClass(HitDie.D8)
    object Barbarian: CharacterClass(HitDie.D12)
    object Paladin: CharacterClass(HitDie.D10)
    object Ranger: CharacterClass(HitDie.D10)
    object Rogue: CharacterClass(HitDie.D8)
    object Warlock: CharacterClass(HitDie.D8)
    object Monk: CharacterClass(HitDie.D8)
    object Sorcerer: CharacterClass(HitDie.D6)
    object Bard: CharacterClass(HitDie.D8)
    object Wizard: CharacterClass(HitDie.D8)
}

data class Character(
    val name: String,
    val characterClass: CharacterClass,
    val stats : StatBlock,
    val level: Int = 1,
) {
    companion object
}

data class HitDie(val numberOfFaces: Int) {
    companion object {
        val D6 = HitDie(6)
        val D8 = HitDie(8)
        val D10 = HitDie(10)
        val D12 = HitDie(12)
    }
}
