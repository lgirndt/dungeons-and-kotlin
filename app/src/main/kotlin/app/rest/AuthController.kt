package io.dungeons.app.rest

import io.dungeons.app.rest.dto.AuthResponse
import io.dungeons.app.rest.dto.AuthenticationRequest
import io.dungeons.app.security.JwtService
import io.dungeons.domain.player.PlayerRequest
import io.dungeons.domain.player.RegisterPlayerUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }

    @PostMapping("/register")
    fun register(@RequestBody playerRequest: PlayerRequest) {
        val hashedPassword : String = passwordEncoder.encode(playerRequest.password)
            ?: error("Password encoding failed")

        val playerWithHashedPasswd = playerRequest.copy(
            password = hashedPassword
        )
        registerPlayerUseCase.execute(playerWithHashedPasswd)
    }
}
