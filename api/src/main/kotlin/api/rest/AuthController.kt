package io.dungeons.api.rest

import io.dungeons.api.rest.dto.AuthResponse
import io.dungeons.api.rest.dto.AuthenticationRequest
import io.dungeons.api.security.JwtService
import io.dungeons.domain.player.PlayerAlreadyExistsException
import io.dungeons.port.usecases.PlayerRequest
import io.dungeons.port.usecases.RegisterPlayerUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.status
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsService,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    private val registerPlayerUseCase: RegisterPlayerUseCase,
) {
    @PostMapping("/login")
    fun login(@RequestBody authenticationRequest: AuthenticationRequest): ResponseEntity<AuthResponse> = try {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                authenticationRequest.username,
                authenticationRequest.password,
            ),
        )

        val userDetails = userDetailsService.loadUserByUsername(authenticationRequest.username)
        val token = jwtService.generateToken(userDetails)

        ResponseEntity.ok(AuthResponse(accessToken = token))
    } catch (_: BadCredentialsException) {
        logger.error { "Invalid credentials for user: ${authenticationRequest.username}" }
        status(HttpStatus.UNAUTHORIZED).build()
    }

    @PostMapping("/register")
    fun register(@RequestBody playerRequest: PlayerRequest): ResponseEntity<Any> {
        val hashedPassword: String = passwordEncoder.encode(playerRequest.password)
            ?: return status(HttpStatus.BAD_REQUEST).build()

        val playerWithHashedPasswd = playerRequest.copy(password = hashedPassword)

        return registerPlayerUseCase.execute(playerWithHashedPasswd).fold(
            onSuccess = { status(HttpStatus.CREATED).body(Any()) },
            onFailure = { exception ->
                logger.error(exception) { "Failed to register player '${playerRequest.name}'" }
                if (exception is PlayerAlreadyExistsException) {
                    // we don't expose that the player already exists for security reasons
                    status(HttpStatus.CREATED).build()
                } else {
                    status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
            },
        )
    }
}
