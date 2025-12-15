package io.dungeons.api.security

import io.dungeons.domain.player.Player
import io.dungeons.domain.player.PlayerRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import kotlin.jvm.optionals.getOrElse

class PlayerDetailsService(private val playerRepository: PlayerRepository) : UserDetailsService {
    override fun loadUserByUsername(username: String): UserDetails {
        val player : Player = playerRepository.findByName(username).getOrElse {
            throw UsernameNotFoundException(username)
        }
        return PlayerDetails(
            playerId = player.id.toUUID(),
            username = player.name,
            password = player.hashedPassword,
            authorities = listOf(
                SimpleGrantedAuthority("ROLE_USER")
            )
        )
    }
}