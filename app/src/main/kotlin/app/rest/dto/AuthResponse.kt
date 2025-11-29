package io.dungeons.app.rest.dto

data class AuthResponse(
    val accessToken: String,
    val tokenType: String = "Bearer"
)
