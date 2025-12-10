package io.dungeons.app.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Profile
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Profile("dev")
class DevTokenAuthenticationFilter(private val userDetailsService: UserDetailsService) : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)

        // Check if it's the dev token
        if (token == DEV_TOKEN && SecurityContextHolder.getContext().authentication == null) {
            @Suppress("TooGenericExceptionCaught")
            try {
                val userDetails = userDetailsService.loadUserByUsername(DEFAULT_USERNAME)
                val authToken = UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.authorities,
                )
                authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = authToken

                if (logger.isDebugEnabled) {
                    logger.debug("Authenticated request using dev token for user: ${DEFAULT_USERNAME}")
                }
            } catch (e: Exception) {
                logger.warn("Failed to authenticate with dev token: {}", e)
            }
        }

        filterChain.doFilter(request, response)
    }

    companion object {
        private const val DEV_TOKEN = "this-is-our-dev-token"
        private const val DEFAULT_USERNAME = "user"
    }
}
