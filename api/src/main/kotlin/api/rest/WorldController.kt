package io.dungeons.api.rest

import io.dungeons.api.rest.dto.WorldIdResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

@RestController
class WorldController {
    // TODO: Implement world creation logic
    @PostMapping("/world")
    fun createWorld(@AuthenticationPrincipal user: User): WorldIdResponse {
        logger.info { "Player ${user.username} was provided" }
        return WorldIdResponse("world-id-placeholder")
    }
}
