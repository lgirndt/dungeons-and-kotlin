package io.dungeons.app.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

data class PlayerDetails(
    val playerId: UUID,
    private val username: String,
    private val password: String,
    private val authorities: Collection<GrantedAuthority>,
    private val enabled: Boolean = true,
    private val accountNonExpired: Boolean = true,
    private val credentialsNonExpired: Boolean = true,
    private val accountNonLocked: Boolean = true
) : UserDetails {
    override fun getAuthorities() = authorities
    override fun getPassword() = password
    override fun getUsername() = username
    override fun isEnabled() = enabled
    override fun isAccountNonExpired() = accountNonExpired
    override fun isCredentialsNonExpired() = credentialsNonExpired
    override fun isAccountNonLocked() = accountNonLocked
}
