package io.dungeons.domain

import io.dungeons.domain.core.Id

data class PlayerCharacterData(val level: Int, val weapon: Weapon)

class PlayerCharacter(
    id: Id<Creature> = Id.Companion.generate(),
    private val data: PlayerCharacterData,
    private val classFeatures: ClassFeatures,
    core: CreatureData,
) : Creature(id, core) {
    val proficiencyBonus: ProficiencyBonus
        get() = ProficiencyBonus.fromLevel(data.level)

    override val weapon: Weapon
        get() = data.weapon

    override val attackModifier: Int
        get() {
            val proficiencyModifier = if (classFeatures.isProficientWith(weapon)) {
                proficiencyBonus
            } else {
                ProficiencyBonus.None
            }

            val attackStat = weapon.whichStat(core.stats)
            val hitRoll = attackStat.modifier + proficiencyModifier.toInt()
            return hitRoll
        }

    fun copy(
        core: CreatureData = this.core,
        data: PlayerCharacterData = this.data,
        classFeatures: ClassFeatures = this.classFeatures,
    ): PlayerCharacter = PlayerCharacter(
        data = data,
        core = core,
        classFeatures = classFeatures,
    )
}
