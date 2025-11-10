package org.example.experiment

import org.example.AttackOutcome
import org.example.Attackable
import org.example.Attacker
import org.example.CharacterClass
import org.example.DamageModifiers
import org.example.DieRoll
import org.example.ProficiencyBonus
import org.example.RollModifier
import org.example.StatBlock
import org.example.Weapon

data class CoreEntityData (
    val name: String,
    val stats: StatBlock,
    var hitPoints: Int,
    val armourClass: Int,
    val damageModifiers: DamageModifiers,
)

abstract class CoreEntity (
    protected val core: CoreEntityData,
) {
    protected abstract val weapon : Weapon
    protected abstract val attackModifier: Int
    protected open fun  isCriticalHit(hitRoll: DieRoll): Boolean {
        return hitRoll.value == 20
    }

    internal fun asAttacker() : Attacker {
        val entity = this
        return object : Attacker {
            override val weapon = entity.weapon
            override val position = org.example.Coordinate(0,0)
            override val stats: StatBlock
                get() = core.stats
            override fun applyAttackModifiers(): Int = entity.attackModifier
            override fun isCriticalHit(hitRoll: DieRoll): Boolean =
                entity.isCriticalHit(hitRoll)
        }
    }

    fun asAttackable() : Attackable {
        return object : Attackable {
            override val armourClass: Int
                get() = core.armourClass
            override val position = org.example.Coordinate(0,0)
            override val damageModifiers: DamageModifiers
                get() = core.damageModifiers
            override var hitPoints: Int
                get() = core.hitPoints
                set(value) {
                    core.hitPoints = value
                }
        }
    }

    fun attack(opponent: Attackable, rollModifier: RollModifier ) : AttackOutcome {
        val attacker = asAttacker()
        return org.example.attack(attacker, opponent, rollModifier)
    }
}

data class PlayerCharacterData(
    val proficiencyBonus: ProficiencyBonus,
    val weapon: Weapon,
)

class PlayerCharacter(
    private val data: PlayerCharacterData,
    private val classFeatures : CharacterClass,
    core: CoreEntityData,
) : CoreEntity(core) {

    fun copy(
        core: CoreEntityData = this.core,
        data: PlayerCharacterData = this.data,
        classFeatures : CharacterClass = this.classFeatures
    ): PlayerCharacter {
        return PlayerCharacter(
            data = data,
            core = core,
            classFeatures = classFeatures
        )
    }

    override val weapon: Weapon
        get() = data.weapon

    override val attackModifier: Int
        get() {
            val proficiencyModifier = if (classFeatures.isProficientWith(weapon))
                data.proficiencyBonus
            else
                ProficiencyBonus.None

            val attackStat = weapon.whichStat(core.stats)
            val hitRoll = attackStat.modifier + proficiencyModifier.toInt()
            return hitRoll
        }
}

data class MonsterData(
    val monsterType: String,
)

//class Monster (
//    private val data: MonsterData,
//    creatureData: CoreEntityData) : CoreEntity(creatureData) {
//
//}