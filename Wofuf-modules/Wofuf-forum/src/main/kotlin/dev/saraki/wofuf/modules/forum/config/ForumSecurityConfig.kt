package dev.saraki.wofuf.modules.forum.config

import dev.saraki.wofuf.auth.infra.JwtAuthFilter
import dev.saraki.wofuf.modules.forum.infra.security.ForumJwtAuthFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * 论坛模块安全配置
 * 使用统一的 JWT 认证过滤器
 *
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/4/9
 */
@Configuration
@EnableWebSecurity
class ForumSecurityConfig {

    @Autowired
    private lateinit var jwtAuthFilter: JwtAuthFilter

    @Autowired
    private lateinit var forumJwtAuthFilter: ForumJwtAuthFilter

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
                    // 图片列表需要认证（后端根据登录用户返回对应图片）
                    .requestMatchers("/api/v1/forum/images/list").authenticated()
                    // 获取图片公开（查看他人帖子中的图片）
                    .requestMatchers(ForumApiConstantV1.Images.BY_MD5).permitAll()
                    // 管理员接口需要认证 (权限由 PermissionAspect 检查)
                    .requestMatchers(ForumApiConstantV1.Admin.ROOT + "/**").authenticated()
                    // 创建帖子需要认证
                    .requestMatchers(ForumApiConstantV1.Posts.ROOT).authenticated()
                    // 评论相关需要认证
                    .requestMatchers(ForumApiConstantV1.Comments.ROOT).authenticated()
                    // 点赞相关需要认证
                    .requestMatchers(ForumApiConstantV1.Posts.UPVOTE).authenticated()
                    .requestMatchers(ForumApiConstantV1.Posts.DOWNVOTE).authenticated()
                    .requestMatchers(ForumApiConstantV1.Posts.UNVOTE).authenticated()
                    .requestMatchers(ForumApiConstantV1.Comments.UPVOTE).authenticated()
                    .requestMatchers(ForumApiConstantV1.Comments.DOWNVOTE).authenticated()
                    .requestMatchers(ForumApiConstantV1.Comments.UNVOTE).authenticated()
                    // 登出需要认证
                    .requestMatchers("/api/v1/forum/members/logout").authenticated()
                    // 其他接口公开访问
                    .requestMatchers(ForumApiConstantV1.Members.ROOT + "/**").permitAll()
                    .requestMatchers(ForumApiConstantV1.Posts.ROOT + "/**").permitAll()
                    .requestMatchers(ForumApiConstantV1.Comments.ROOT + "/**").permitAll()
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(forumJwtAuthFilter, JwtAuthFilter::class.java)

        return http.build()
    }
}
