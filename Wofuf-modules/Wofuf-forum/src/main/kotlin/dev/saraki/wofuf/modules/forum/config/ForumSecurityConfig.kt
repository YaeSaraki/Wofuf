package dev.saraki.wofuf.modules.forum.config

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/16 14:50
 *   @description:
 */
import dev.saraki.wofuf.modules.forum.infra.auth.springSecurity.JwtUserAuthenticationTokenFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter


@Configuration
@EnableWebSecurity
class ForumSecurityConfig {

    @Autowired
    private lateinit var jwtUserAuthenticationTokenFilter: JwtUserAuthenticationTokenFilter

    @Bean
    @Order(1)
    fun forumFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher(ForumApiConstantV1.Base.ROOT + "/**")
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    // 图片上传需要认证
                    .requestMatchers(ForumApiConstantV1.Images.UPLOAD).authenticated()
                    // 其他接口公开访问
                    .requestMatchers(ForumApiConstantV1.Members.ROOT + "/**").permitAll()
                    .requestMatchers(ForumApiConstantV1.Posts.ROOT + "/**").permitAll()
                    .requestMatchers(ForumApiConstantV1.Comments.ROOT + "/**").permitAll()
                    .requestMatchers(ForumApiConstantV1.Images.ROOT + "/**").permitAll()
            }
        http.addFilterBefore(jwtUserAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }
}