package dev.saraki.wofuf.auth.config

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/16 14:50
 *   @description:
 */
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {
    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoder()

    @Bean
    @Order(999)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/swagger-ui/**")
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .anyRequest().permitAll()
            }
        return http.build()
    }

    @Bean
    fun authenticationManager(
        authenticationConfiguration: AuthenticationConfiguration
    ): AuthenticationManager = authenticationConfiguration.authenticationManager
}