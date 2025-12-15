package io.dungeons.api.rest.dto

data class AuthResponse(val accessToken: String, val tokenType: String = "Bearer")
