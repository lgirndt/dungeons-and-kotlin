package io.dungeons.api.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*
import javax.crypto.SecretKey
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.toJavaInstant
import kotlin.time.toKotlinInstant

@Service
class JwtService(private val jwtProperties: JwtProperties, private val clock: Clock) {
    private val secretKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    fun generateToken(userDetails: UserDetails): String {
        val claims = mutableMapOf<String, Any>()
        claims["roles"] = userDetails.authorities.map { it.authority }

        return createToken(claims, userDetails.username)
    }

    private fun createToken(claims: Map<String, Any>, subject: String): String {
        val now = clock.now()
        val expirationInstant = now + jwtProperties.expiration

        return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(now.toJavaDate())
            .expiration(expirationInstant.toJavaDate())
            .signWith(secretKey)
            .compact()
    }

    fun extractUsername(token: String): String = extractClaim(token) { it.subject }

    fun extractExpiration(token: String): Instant = extractClaim(token) {
        it.expiration.toInstant().toKotlinInstant()
    }

    fun <T> extractClaim(token: String, claimsResolver: (Claims) -> T): T {
        val claims = extractAllClaims(token)
        return claimsResolver(claims)
    }

    private fun extractAllClaims(token: String): Claims = Jwts.parser()
        .clock { clock.asJwtClock() }
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .payload

    fun isTokenValid(token: String, userDetails: UserDetails): Boolean {
        val username = extractUsername(token)
        return username == userDetails.username && !isTokenExpired(token)
    }

    private fun isTokenExpired(token: String): Boolean = extractExpiration(token) < clock.now()
}

internal fun Instant.toJavaDate() = Date.from(this.toJavaInstant())

private fun Clock.asJwtClock(): java.util.Date {
    val now = this.now()
    return Date.from(now.toJavaInstant())
}
