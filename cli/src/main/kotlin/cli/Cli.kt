package io.dungeons.cli

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.ajalt.clikt.core.main
import io.dungeons.cli.screen.DetailsScreen
import io.dungeons.cli.screen.MyScreen
import io.dungeons.cli.screen.PickAdventureScreen
import io.dungeons.cli.screen.PickGameScreen
import io.dungeons.cli.screen.RoomScreen
import io.dungeons.cli.screen.Screen
import io.dungeons.cli.screen.ScreenTransition
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestClient
import kotlin.time.Clock

typealias ScreenMap = Map<ScreenTransition, Screen<ScreenTransition>>

@SpringBootApplication(scanBasePackages = ["io.dungeons"])
class Cli {
    private val logger = LoggerFactory.getLogger(Cli::class.java)

    @Bean
    fun clock() = Clock.System

    @Bean
    fun objectMapper() = jacksonObjectMapper()

    @Bean
    fun screens(
        myScreen: MyScreen,
        detailsScreen: DetailsScreen,
        pickAdventureScreen: PickAdventureScreen,
        pickGameScreen: PickGameScreen,
        roomScreen: RoomScreen,
    ): ScreenMap {
        // It makes much more sense to build this lazy
        return listOf(
            myScreen,
            detailsScreen,
            pickAdventureScreen,
            pickGameScreen,
            roomScreen,
        ).associateBy { it.ownTransition }
    }

    @Bean
    fun restClient() =
        RestClient
            .builder()
            .baseUrl("TODO")
            .requestInterceptor { request, bytes, execution ->  execution.execute(request, bytes) }
            .build()

    @Bean
    fun gameLoop(screens: ScreenMap, gameStateHolder: GameStateHolder) =
        GameLoop(
            screens = screens,
            gameStateHolder = gameStateHolder,
        )

    @Bean
    fun commandLineRunner(mainCommand: MainCommand) = CommandLineRunner { args ->
        mainCommand.main(args)
    }


}

fun main(args: Array<String>) {
    // Otherwise the VirtualTerminal may fail to initialize on some systems
    System.setProperty("java.awt.headless", "false")
    runApplication<Cli>(*args)
}
