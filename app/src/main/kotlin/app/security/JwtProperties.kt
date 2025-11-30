package io.dungeons.app.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Component
@ConfigurationProperties(prefix = "jwt")
class JwtProperties {
    var secret: String = ""
    var expiration: Long = 86400000 // 24 hours in milliseconds

    val expirationAsDuration: Duration
        get() = expiration.toDuration(DurationUnit.MILLISECONDS)
}
