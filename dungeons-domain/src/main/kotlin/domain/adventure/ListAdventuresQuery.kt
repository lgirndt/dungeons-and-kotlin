package io.dungeons.domain.adventure

import org.springframework.stereotype.Component

@Component
class ListAdventuresQuery(private val adventureRepository: AdventureRepository) {
    fun execute(): List<Adventure> = adventureRepository.findAll()
}
