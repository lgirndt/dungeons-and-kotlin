package io.dungeons.cli

import io.dungeons.domain.adventure.AdventureRepository
import io.dungeons.domain.adventure.ListAdventuresQuery
import io.dungeons.domain.narrator.NarrateRoomQuery
import io.dungeons.domain.savegame.MockSaveGameRepository
import io.dungeons.domain.savegame.NewGameUseCase
import io.dungeons.domain.savegame.SaveGameRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DomainConfig {
    @Bean
    fun listAdventuresQuery(adventureRepository: AdventureRepository): ListAdventuresQuery {
        return ListAdventuresQuery(adventureRepository)
    }

//    @Bean
//    fun adventureRepository(): AdventureRepository {
//        return MockAdventureRepository()
//    }

    @Bean
    fun saveGameRepository(): SaveGameRepository {
        return MockSaveGameRepository()
    }

    @Bean
    fun newGameUseCase(saveGameRepository: SaveGameRepository) = NewGameUseCase(saveGameRepository)

    @Bean
    fun narrateRoomQuery(
        saveGameRepository: SaveGameRepository,
        adventureRepository: AdventureRepository,
    ) = NarrateRoomQuery(saveGameRepository, adventureRepository)
}