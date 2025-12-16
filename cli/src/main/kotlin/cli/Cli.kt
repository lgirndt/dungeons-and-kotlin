package io.dungeons.cli

import com.github.ajalt.clikt.core.main
import io.dungeons.cli.screen.DetailsScreen
import io.dungeons.cli.screen.MyScreen
import io.dungeons.cli.screen.PickAdventureScreen
import io.dungeons.cli.screen.PickGameScreen
import io.dungeons.cli.screen.RoomScreen
import io.dungeons.cli.screen.Screen
import io.dungeons.cli.screen.ScreenTransition
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.web.client.RestClient
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.kotlinModule
import kotlin.time.Clock

typealias ScreenMap = Map<ScreenTransition, Screen<ScreenTransition>>

@SpringBootApplication(scanBasePackages = ["io.dungeons"])
class Cli {
    @Bean
    fun clock() = Clock.System

    @Bean
    fun objectMapper(): JsonMapper = JsonMapper.builder()
        .addModule(kotlinModule())
        .build()

    @Bean
    fun screens(
        myScreen: MyScreen,
        detailsScreen: DetailsScreen,
        pickAdventureScreen: PickAdventureScreen,
        pickGameScreen: PickGameScreen,
        roomScreen: RoomScreen,
    ): ScreenMap {
        // It makes much more sense to build this lazily
        return listOf(
            myScreen,
            detailsScreen,
            pickAdventureScreen,
            pickGameScreen,
            roomScreen,
        ).associateBy { it.ownTransition }
    }

    @Bean
    fun restClient(
        gameStateHolder: GameStateHolder,
        @Value("\${dungeons.api.base_url}") baseUrl: String
    ): RestClient =
        RestClient
            .builder()
            .baseUrl(baseUrl)
            .requestInterceptor(TokenProvidingInterceptor(gameStateHolder))
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

class TokenProvidingInterceptor(private val gameStateHolder: GameStateHolder) : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        if(gameStateHolder.gameState.authToken != null) {
            request.headers.add("Authorization", "Bearer ${gameStateHolder.gameState.authToken}")
        }
        return execution.execute(request, body)
    }
}

fun main(args: Array<String>) {
    // Otherwise the VirtualTerminal may fail to initialize on some systems
    System.setProperty("java.awt.headless", "false")
    runApplication<Cli>(*args)
}
