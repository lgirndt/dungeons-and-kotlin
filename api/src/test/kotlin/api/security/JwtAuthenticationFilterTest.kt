package io.dungeons.api.security

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import kotlin.test.assertEquals

private const val TOKEN = "my.jwt.token"

class JwtAuthenticationFilterTest {
    private val jwtService = mockk<JwtService>()
    private val userDetailsService = mockk<UserDetailsService>()
    private val filter = JwtAuthenticationFilter(jwtService, userDetailsService)

    private val request = mockk<HttpServletRequest>(relaxed = true)
    private val response = mockk<HttpServletResponse>(relaxed = true)
    private val filterChain = mockk<FilterChain>(relaxed = true)

    private val testUser = User.builder()
        .username("testuser")
        .password("password")
        .authorities(listOf(SimpleGrantedAuthority("ROLE_USER")))
        .build()

    @BeforeEach
    fun setUp() {
        // Clear security context before each test
        SecurityContextHolder.clearContext()

        // Mock request attributes for OncePerRequestFilter
        every { request.getAttribute(any()) } returns null
        every { request.setAttribute(any(), any()) } returns Unit
    }

    @AfterEach
    fun tearDown() {
        // Clear security context after each test
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `filter passes through when no Authorization header is present`() {
        every { request.getHeader("Authorization") } returns null

        filter.doFilter(request, response, filterChain)

        verify { filterChain.doFilter(request, response) }
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `filter passes through when Authorization header does not start with Bearer`() {
        every { request.getHeader("Authorization") } returns "Basic sometoken"

        filter.doFilter(request, response, filterChain)

        verify { filterChain.doFilter(request, response) }
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `filter sets authentication when valid token is provided`() {
        val token = "valid.jwt.token"
        every { request.getHeader("Authorization") } returns "Bearer $token"
        every { jwtService.extractUsername(token) } returns "testuser"
        every { userDetailsService.loadUserByUsername("testuser") } returns testUser
        every { jwtService.isTokenValid(token, testUser) } returns true

        filter.doFilter(request, response, filterChain)

        verify { filterChain.doFilter(request, response) }

        val authentication = SecurityContextHolder.getContext().authentication
        assertNotNull(authentication)
        assertEquals("testuser", authentication.name)
        assertEquals(1, authentication.authorities.size)
        assertEquals("ROLE_USER", authentication.authorities.first().authority)
    }

    @Test
    fun `filter does not set authentication when token is invalid`() {
        every { request.getHeader("Authorization") } returns "Bearer $TOKEN"
        every { jwtService.extractUsername(TOKEN) } returns "testuser"
        every { userDetailsService.loadUserByUsername("testuser") } returns testUser
        every { jwtService.isTokenValid(TOKEN, testUser) } returns false

        filter.doFilter(request, response, filterChain)

        verify { filterChain.doFilter(request, response) }
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `filter skips authentication when context already has authentication`() {
        // Set up existing authentication
        val existingAuth = mockk<org.springframework.security.core.Authentication>()
        SecurityContextHolder.getContext().authentication = existingAuth

        every { request.getHeader("Authorization") } returns "Bearer $TOKEN"
        every { jwtService.extractUsername(TOKEN) } returns "testuser"

        filter.doFilter(request, response, filterChain)

        verify { filterChain.doFilter(request, response) }
        verify(exactly = 0) { userDetailsService.loadUserByUsername(any()) }
        verify(exactly = 0) { jwtService.isTokenValid(any(), any()) }

        // Authentication should remain the existing one
        assertEquals(existingAuth, SecurityContextHolder.getContext().authentication)
    }

    @Test
    fun `filter correctly extracts token from Bearer prefix`() {
        every { request.getHeader("Authorization") } returns "Bearer $TOKEN"
        every { jwtService.extractUsername(TOKEN) } returns "testuser"
        every { userDetailsService.loadUserByUsername("testuser") } returns testUser
        every { jwtService.isTokenValid(TOKEN, testUser) } returns true

        filter.doFilter(request, response, filterChain)

        verify { jwtService.extractUsername(TOKEN) }
        verify { jwtService.isTokenValid(TOKEN, testUser) }
    }

    @Test
    fun `filter sets authentication details from request`() {
        every { request.getHeader("Authorization") } returns "Bearer $TOKEN"
        every { jwtService.extractUsername(TOKEN) } returns "testuser"
        every { userDetailsService.loadUserByUsername("testuser") } returns testUser
        every { jwtService.isTokenValid(TOKEN, testUser) } returns true

        filter.doFilter(request, response, filterChain)

        val authentication = SecurityContextHolder.getContext().authentication
        assertNotNull(authentication)
        assertNotNull(authentication.details)
    }

    @Test
    fun `filter handles user with multiple roles`() {
        val adminUser = User.builder()
            .username("admin")
            .password("password")
            .authorities(
                listOf(
                    SimpleGrantedAuthority("ROLE_ADMIN"),
                    SimpleGrantedAuthority("ROLE_USER"),
                ),
            )
            .build()

        every { request.getHeader("Authorization") } returns "Bearer $TOKEN"
        every { jwtService.extractUsername(TOKEN) } returns "admin"
        every { userDetailsService.loadUserByUsername("admin") } returns adminUser
        every { jwtService.isTokenValid(TOKEN, adminUser) } returns true

        filter.doFilter(request, response, filterChain)

        val authentication = SecurityContextHolder.getContext().authentication
        assertNotNull(authentication)
        assertEquals("admin", authentication.name)
        assertEquals(2, authentication.authorities.size)
    }
}
