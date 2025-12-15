package io.dungeons.api.rest

import io.dungeons.api.rest.dto.WorldIdResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class WorldController {
    private val logger = LoggerFactory.getLogger(WorldController::class.java)

    // TODO: Implement world creation logic
    @PostMapping("/world")
    fun createWorld(@AuthenticationPrincipal user: User): WorldIdResponse {
        logger.info("Player ${user.username} was provided")
        return WorldIdResponse("world-id-placeholder")
    }
}
