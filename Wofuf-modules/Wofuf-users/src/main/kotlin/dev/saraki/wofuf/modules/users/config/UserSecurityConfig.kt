package dev.saraki.wofuf.modules.users.config

import dev.saraki.wofuf.auth.infra.JwtAuthFilter
import dev.saraki.wofuf.modules.users.infra.auth.springSecurity.JwtUserAuthenticationTokenFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.DELETE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpMethod.POST
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * 用户模块安全配置
 *
 * 支持两种部署模式：
 * 1. 单体部署 + Redis 会话检查：使用 JwtUserAuthenticationTokenFilter
 * 2. 分布式部署：使用 JwtAuthFilter（统一）
 *
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/2/16 14:50
 */
@Configuration
@EnableWebSecurity
class UserSecurityConfig {

    @Autowired(required = false)
    private var jwtUserAuthenticationTokenFilter: JwtUserAuthenticationTokenFilter? = null

    @Autowired
    private lateinit var jwtAuthFilter: JwtAuthFilter

    @Bean
    fun userFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // 1. 仅匹配用户模块路径
            .securityMatcher(UserApiConstantV1.Base.ROOT)
            // 2. 禁用 CSRF（JWT 无状态认证无需 CSRF）
            .csrf { it.disable() }
            // 3. 禁用 Session（JWT 认证，无状态）
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            // 4. 授权规则配置
            .authorizeHttpRequests { auth ->
                auth
                    // 公开接口（无需认证）
                    .requestMatchers(POST,   UserApiConstantV1.Base.ROOT).permitAll()
                    .requestMatchers(POST,    UserApiConstantV1.Me.SESSIONS).permitAll()
                    // 需要认证的接口
                    .requestMatchers(GET,    UserApiConstantV1.Base.ME).authenticated()
                    .requestMatchers(POST,    UserApiConstantV1.Me.TOKENS).authenticated()
                    .requestMatchers(DELETE, UserApiConstantV1.Base.ROOT).authenticated()
                    .requestMatchers(DELETE, UserApiConstantV1.Me.SESSIONS).authenticated()

                    // 禁止访问的接口
                    .requestMatchers(UserApiConstantV1.Base.BY_USERNAME).denyAll()
                    .requestMatchers(UserApiConstantV1.Base.BY_ID).denyAll()

                    // 兜底规则：其他用户模块接口需认证
                    .anyRequest().authenticated()
            }

        // 5. 添加 JWT 过滤器（优先使用会话检查 filter，否则使用统一 filter）
        if (jwtUserAuthenticationTokenFilter != null) {
            // 单体部署 + Redis 会话检查模式
            http.addFilterBefore(
                jwtUserAuthenticationTokenFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )
        } else {
            // 分布式部署或单体部署（无会话检查）模式
            http.addFilterBefore(
                jwtAuthFilter,
                UsernamePasswordAuthenticationFilter::class.java
            )
        }

        return http.build()
    }
}
