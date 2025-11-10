package org.example

import org.example.spell.AttackSpell
import org.example.spell.Caster
import org.example.spell.SpellLevel
import org.example.spell.castAttackSpell


data class Character(
    val name: String,
    val characterClass: CharacterClass,
    val stats: StatBlock,
    val level: Int = 1,
    override val damageModifiers: DamageModifiers = DamageModifiers.NONE,
    var currentWeapon: Weapon? = null,
    override var hitPoints: Int,
    val armour: (StatBlock) -> Int,
    override val position: Coordinate = Coordinate(0, 0),
) : Attackable {
    val proficiencyBonus: ProficiencyBonus
        get() = ProficiencyBonus.fromLevel(level)

    private val spellCasting = characterClass.toSpellCaster(stats, level)

    override val armourClass: Int get() = armour(stats)

    fun equip(weapon: Weapon) {
        this.currentWeapon = weapon
    }

    fun attack(opponent: Attackable, rollModifier: RollModifier = RollModifier.NORMAL): AttackOutcome {
        // to hit
        val existingWeapon = currentWeapon ?: return AttackOutcome.MISS
        val character = this
        return attack(object : Attacker {
            override val weapon = existingWeapon
            override val position: Coordinate = character.position
            override val stats: StatBlock = character.stats
            override fun applyAttackModifiers(): Int = character.applyAttackModifiers(existingWeapon)
            override fun isCriticalHit(hitRoll: DieRoll): Boolean = character.isCriticalHit(hitRoll)
        }, opponent, rollModifier)
    }


    private fun applyAttackModifiers(weapon: Weapon): Int {
        val proficiencyModifier = if (isProficientWith(weapon)) proficiencyBonus else ProficiencyBonus.None
        val attackStat = weapon.whichStat(stats)

        val hitRoll = attackStat.modifier + proficiencyModifier.toInt()
        return hitRoll
    }

    override fun receiveDamage(amount: Int, damageType: DamageType): Int {
        val adjustedAmount = when (damageType) {
            in damageModifiers.immunities -> 0
            in damageModifiers.resistances -> amount / 2
            in damageModifiers.vulnerabilities -> amount * 2
            else -> amount
        }

        hitPoints = (hitPoints - adjustedAmount).coerceAtLeast(0)

        return adjustedAmount
    }

    private fun isProficientWith(weapon: Weapon): Boolean {
        return characterClass.isProficientWith(weapon)
    }

    private fun isCriticalHit(hitRoll: DieRoll): Boolean {
        // TODO needs to use class
        return hitRoll.value == 20
    }

    fun castSpellAttack(
        spell: AttackSpell,
        level: SpellLevel,
        opponent: Attackable,
        rollModifier: RollModifier = RollModifier.NORMAL) : AttackOutcome? {
        val spellCasting = this.spellCasting ?: return null
        val character = this
        val caster = object : Caster {
            override val spellCastingAbility: Stat = spellCasting.ability(character.stats)
            override val proficiencyBonus: ProficiencyBonus = character.proficiencyBonus
            override val position: Coordinate = character.position
            override val stats: StatBlock = character.stats
        }
        return castAttackSpell(
            caster,
            opponent,
            spell,
            level,
            rollModifier
        )
    }

}
