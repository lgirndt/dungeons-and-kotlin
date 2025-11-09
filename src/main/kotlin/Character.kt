package org.example


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
    val proficiencyBonus: ProficiencyBonus get() = ProficiencyBonus.fromLevel(level)

    override val armourClass: Int get() = armour(stats)

    fun equip(weapon: Weapon) {
        this.currentWeapon = weapon
    }

    fun asAttacker(): Attacker {
        val parent = this
        return object: Attacker {
            override val currentWeapon: Weapon? = parent.currentWeapon
            override val position: Coordinate = parent.position
            override val stats: StatBlock = parent.stats
            override fun applyAttackModifiers(weapon: Weapon): Int = parent.applyAttackModifiers(weapon)
            override fun isCriticalHit(hitRoll: DieRoll): Boolean = parent.isCriticalHit(hitRoll)
        }
    }

    fun attack(opponent: Attackable, rollModifier: RollModifier = RollModifier.NORMAL): AttackOutcome {
        return attack(asAttacker(), opponent, rollModifier)
    }

    private fun applyAttackModifiers(weapon: Weapon): Int {
        val proficiencyModifier = if (isProficientWith(weapon)) proficiencyBonus else ProficiencyBonus.None
        val modifier = weapon.receiveModifier { query -> query(stats) }

        val hitRoll = modifier + proficiencyModifier.toInt()
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

}
