package org.example

data class PlayerCharacterData(
    val level: Int,
    val weapon: Weapon,
)

class PlayerCharacter(
    id: Id<CoreEntity> = Id.generate(),
    private val data: PlayerCharacterData,
    private val classFeatures : ClassFeatures,
    core: CoreEntityData,
) : CoreEntity(id,core) {

    companion object {}

    val proficiencyBonus: ProficiencyBonus
        get() = ProficiencyBonus.fromLevel(data.level)

    fun copy(
        core: CoreEntityData = this.core,
        data: PlayerCharacterData = this.data,
        classFeatures : ClassFeatures = this.classFeatures
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
                proficiencyBonus
            else
                ProficiencyBonus.None

            val attackStat = weapon.whichStat(core.stats)
            val hitRoll = attackStat.modifier + proficiencyModifier.toInt()
            return hitRoll
        }
}