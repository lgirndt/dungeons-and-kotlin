package io.dungeons.api.rest

import io.dungeons.api.rest.dto.AuthenticationRequest
import io.dungeons.api.security.JwtService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService

class AuthControllerTest {
    private val authenticationManager = mockk<AuthenticationManager>()
    private val userDetailsService = mockk<UserDetailsService>()
    private val jwtService = mockk<JwtService>()

    private val authController = AuthController(
        authenticationManager,
        userDetailsService,
        jwtService,
    )

    private val testUser = User.builder()
        .username("testuser")
        .password("password")
        .authorities(listOf(SimpleGrantedAuthority("ROLE_USER")))
        .build()

    @Test
    fun `login returns token for valid credentials`() {
        val authenticationRequest = AuthenticationRequest("testuser", "password")
        val authentication = mockk<Authentication>()

        every {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken("testuser", "password"),
            )
        } returns authentication

        every { userDetailsService.loadUserByUsername("testuser") } returns testUser
        every { jwtService.generateToken(testUser) } returns "valid.jwt.token"

        val response = authController.login(authenticationRequest)

        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals("valid.jwt.token", response.body?.accessToken)
        assertEquals("Bearer", response.body?.tokenType)

        verify { authenticationManager.authenticate(any()) }
        verify { userDetailsService.loadUserByUsername("testuser") }
        verify { jwtService.generateToken(testUser) }
    }

    @Test
    fun `login returns 401 for invalid credentials`() {
        val authenticationRequest = AuthenticationRequest("testuser", "wrongpassword")

        every {
            authenticationManager.authenticate(any())
        } throws BadCredentialsException("Bad credentials")

        val response = authController.login(authenticationRequest)

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
        assertEquals(null, response.body)

        verify { authenticationManager.authenticate(any()) }
    }

    @Test
    fun `login returns 401 for non-existent user`() {
        val authenticationRequest = AuthenticationRequest("nonexistent", "password")

        every {
            authenticationManager.authenticate(any())
        } throws BadCredentialsException("Bad credentials")

        val response = authController.login(authenticationRequest)

        assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)

        verify { authenticationManager.authenticate(any()) }
    }

    @Test
    fun `login generates unique tokens for different users`() {
        val user1Request = AuthenticationRequest("user1", "password1")
        val user2Request = AuthenticationRequest("user2", "password2")

        val user1 = User.builder()
            .username("user1")
            .password("password1")
            .authorities(listOf(SimpleGrantedAuthority("ROLE_USER")))
            .build()

        val user2 = User.builder()
            .username("user2")
            .password("password2")
            .authorities(listOf(SimpleGrantedAuthority("ROLE_USER")))
            .build()

        val auth1 = mockk<Authentication>()
        val auth2 = mockk<Authentication>()

        every {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken("user1", "password1"),
            )
        } returns auth1

        every {
            authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken("user2", "password2"),
            )
        } returns auth2

        every { userDetailsService.loadUserByUsername("user1") } returns user1
        every { userDetailsService.loadUserByUsername("user2") } returns user2
        every { jwtService.generateToken(user1) } returns "token.for.user1"
        every { jwtService.generateToken(user2) } returns "token.for.user2"

        val response1 = authController.login(user1Request)
        val response2 = authController.login(user2Request)

        assertEquals("token.for.user1", response1.body?.accessToken)
        assertEquals("token.for.user2", response2.body?.accessToken)
    }
}
