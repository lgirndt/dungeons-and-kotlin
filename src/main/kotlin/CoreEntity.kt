package org.example

import javax.swing.text.Position

data class CoreEntityData (
    val name: String,
    val stats: StatBlock,
    var hitPoints: Int,
    val armourClass: Int,
    val damageModifiers: DamageModifiers,
    val position: Coordinate
)

abstract class CoreEntity (
    protected val core: CoreEntityData,
) {
    val stats : StatBlock
        get() = core.stats
    val hitPoints : Int
        get() = core.hitPoints

    protected abstract val weapon : Weapon
    protected abstract val attackModifier: Int
    protected open fun  isCriticalHit(hitRoll: DieRoll): Boolean {
        return hitRoll.value == 20
    }

    internal fun asAttacker() : Attacker {
        val entity = this
        return object : Attacker {
            override val weapon = entity.weapon
            override val position = entity.core.position
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
            override val position = core.position
            override val damageModifiers: DamageModifiers
                get() = core.damageModifiers
            override var hitPoints: Int
                get() = core.hitPoints
                set(value) {
                    core.hitPoints = value
                }
        }
    }

    fun attack(opponent: Attackable, rollModifier: RollModifier = RollModifier.NORMAL) : AttackOutcome {
        val attacker = asAttacker()
        return attack(attacker, opponent, rollModifier)
    }
}