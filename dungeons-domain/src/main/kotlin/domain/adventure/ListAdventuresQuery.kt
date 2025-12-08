package io.dungeons.domain.adventure

class ListAdventuresQuery(
    private val adventureRepository: AdventureRepository) {

    fun execute(): List<Adventure> {
        return adventureRepository.findAll()
    }
}