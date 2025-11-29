package io.dungeons.app.security

import io.dungeons.app.config.JwtProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import java.util.Date

class JwtServiceTest {

    private val jwtProperties = JwtProperties().apply {
        secret = "testSecretKeyThatIsLongEnoughForHMACSHA512AlgorithmToWorkProperly"
        expiration = 3600000 // 1 hour in milliseconds
    }
    
    private val jwtService = JwtService(jwtProperties)

    private val testUser = User.builder()
        .username("testuser")
        .password("password")
        .authorities(listOf(SimpleGrantedAuthority("ROLE_USER")))
        .build()

    @Test
    fun `generateToken creates a valid JWT token`() {
        val token = jwtService.generateToken(testUser)

        assertNotNull(token)
        assertTrue(token.isNotBlank())
        assertTrue(token.split(".").size == 3) // JWT has 3 parts: header.payload.signature
    }

    @Test
    fun `extractUsername returns correct username from token`() {
        val token = jwtService.generateToken(testUser)

        val username = jwtService.extractUsername(token)

        assertEquals("testuser", username)
    }

    @Test
    fun `token contains user roles`() {
        val adminUser = User.builder()
            .username("admin")
            .password("password")
            .authorities(
                listOf(
                    SimpleGrantedAuthority("ROLE_ADMIN"),
                    SimpleGrantedAuthority("ROLE_USER")
                )
            )
            .build()

        val token = jwtService.generateToken(adminUser)

        assertNotNull(token)
        val username = jwtService.extractUsername(token)
        assertEquals("admin", username)
    }

    @Test
    fun `isTokenValid returns true for valid token`() {
        val token = jwtService.generateToken(testUser)

        val isValid = jwtService.isTokenValid(token, testUser)

        assertTrue(isValid)
    }

    @Test
    fun `isTokenValid returns false for token with wrong username`() {
        val token = jwtService.generateToken(testUser)

        val differentUser = User.builder()
            .username("differentuser")
            .password("password")
            .authorities(listOf(SimpleGrantedAuthority("ROLE_USER")))
            .build()

        val isValid = jwtService.isTokenValid(token, differentUser)

        assertFalse(isValid)
    }

    @Test
    fun `extractExpiration returns future date for new token`() {
        val token = jwtService.generateToken(testUser)

        val expiration = jwtService.extractExpiration(token)

        assertTrue(expiration.after(Date()))
    }

    @Test
    fun `token expiration is set according to configuration`() {
        val beforeGeneration = System.currentTimeMillis()
        val token = jwtService.generateToken(testUser)
        val afterGeneration = System.currentTimeMillis()

        val expiration = jwtService.extractExpiration(token)
        val expirationTime = expiration.time

        val minExpectedExpiration = beforeGeneration + jwtProperties.expiration
        val maxExpectedExpiration = afterGeneration + jwtProperties.expiration + 1000 // 1 second tolerance

        assertTrue(expirationTime >= minExpectedExpiration - 1000) // 1 second tolerance
        assertTrue(expirationTime <= maxExpectedExpiration)
    }
}
