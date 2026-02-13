package dev.saraki.wofuf.modules.users.config

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/16 14:50
 *   @description:
 */
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


@Configuration
@EnableWebSecurity
class UserSecurityConfig {
    @Autowired
    private lateinit var jwtUserAuthenticationTokenFilter: JwtUserAuthenticationTokenFilter

    @Bean
    fun userFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // 1. 仅匹配用户模块路径（复用编译期常量，无硬编码）
            .securityMatcher(UserApiConstantV1.Base.ROOT)
            // 2. 禁用 CSRF（JWT 无状态认证无需 CSRF）
            .csrf { it.disable() }
            // 3. 禁用 Session（JWT 认证，无状态）
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            // 4. 授权规则配置（复用 UserApiConstantV1 常量，消除硬编码）
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
            // 5. 添加 JWT 过滤器（在 UsernamePasswordAuthenticationFilter 之前）
            .addFilterBefore(jwtUserAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}