package io.dungeons.api.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

@Component
@ConfigurationProperties(prefix = "jwt")
class JwtProperties {
    var secret: String = ""
    var expiration: Duration = 24.hours // 24 hours in milliseconds

    var expirationInSeconds: Long
        get() = expiration.inWholeSeconds
        set(value) {
            expiration = value.seconds
        }
}
