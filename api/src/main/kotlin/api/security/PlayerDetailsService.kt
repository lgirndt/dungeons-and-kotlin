package io.dungeons.api.security

import io.dungeons.domain.player.Player
import io.dungeons.domain.player.PlayerRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import kotlin.jvm.optionals.getOrElse

private fun Player.toPlayerDetails() = PlayerDetails(
    playerId = this.id.toUUID(),
    username = this.name,
    password = this.hashedPassword,
    authorities = listOf(
        SimpleGrantedAuthority("ROLE_USER"),
    ),
)

class PlayerDetailsService(private val playerRepository: PlayerRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String) = playerRepository
        .findByName(username)
        .getOrElse {
            throw UsernameNotFoundException(username)
        }
        .toPlayerDetails()
}
