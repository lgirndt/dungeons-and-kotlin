package io.dungeons.app.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import java.util.Optional

@Configuration
@EnableWebSecurity
class SecurityConfig {
    //    @Bean
//    fun jwtAuthenticationFilter(
//        jwtService: JwtService,
//        userDetailsService: UserDetailsService,
//    ): JwtAuthenticationFilter {
//        return JwtAuthenticationFilter(jwtService, userDetailsService)
//    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter,
        authenticationProvider: AuthenticationProvider,
        devTokenAuthenticationFilter: Optional<DevTokenAuthenticationFilter>,
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers("/api/auth/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated()
            }
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authenticationProvider(authenticationProvider)

        // Add JWT filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        // Add dev token filter before UsernamePasswordAuthenticationFilter (and thus before JWT)
        // This results in filter chain: DevToken -> JWT -> UsernamePassword
        devTokenAuthenticationFilter.ifPresent { devFilter ->
            http.addFilterBefore(devFilter, UsernamePasswordAuthenticationFilter::class.java)
        }

        return http.build()
    }

    @Bean
    fun userDetailsService(): UserDetailsService {
        // TODO: Replace with real user details service
        val user = PlayerDetails(
            username = "user",
            password = passwordEncoder().encode("password") ?: error("Password encoding failed"),
            authorities = listOf(org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")),
            playerId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        )

        val admin = PlayerDetails(
            username = "admin",
            password = passwordEncoder().encode("admin") ?: error("Password encoding failed"),
            authorities = listOf(
                org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_ADMIN"),
                org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")
            ),
            playerId = UUID.fromString("00000000-0000-0000-0000-000000000002")
        )

        return InMemoryUserDetailsManager(user, admin)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun authenticationProvider(
        userDetailsService: UserDetailsService,
        passwordEncoder: PasswordEncoder,
    ): AuthenticationProvider = DaoAuthenticationProvider(userDetailsService).apply {
        setPasswordEncoder(passwordEncoder)
    }

    @Bean
    fun authenticationManager(config: AuthenticationConfiguration): AuthenticationManager = config.authenticationManager
}
