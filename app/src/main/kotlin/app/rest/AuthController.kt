package io.dungeons.app.rest

import io.dungeons.app.rest.dto.AuthResponse
import io.dungeons.app.rest.dto.AuthenticationRequest
import io.dungeons.app.security.JwtService
import io.dungeons.domain.player.PlayerAlreadyExistsException
import io.dungeons.domain.player.PlayerRequest
import io.dungeons.domain.player.RegisterPlayerUseCase
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
        status(HttpStatus.UNAUTHORIZED).build()
    }

    @PostMapping("/register")
    fun register(@RequestBody playerRequest: PlayerRequest) : ResponseEntity<Unit> {
        val hashedPassword: String = passwordEncoder.encode(playerRequest.password)
            ?: return status(HttpStatus.BAD_REQUEST).build()

        val playerWithHashedPasswd = playerRequest.copy(password = hashedPassword)

        return registerPlayerUseCase.execute(playerWithHashedPasswd).fold(
            onSuccess = { status(HttpStatus.CREATED).body()},
            onFailure = { exception ->
                when (exception) {
                    // we don't expose that the player already exists for security reasons
                    is PlayerAlreadyExistsException -> status(HttpStatus.CREATED).build()
                    else -> status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
            }
        )
    }
}
