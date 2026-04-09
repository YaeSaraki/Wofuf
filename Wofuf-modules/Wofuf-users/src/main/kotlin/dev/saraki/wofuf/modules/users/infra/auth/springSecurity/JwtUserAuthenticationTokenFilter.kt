package dev.saraki.wofuf.modules.users.infra.auth.springSecurity

import dev.saraki.wofuf.modules.users.services.auth.UserAuthService
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 用户模块专用 JWT 认证过滤器
 * 负责验证 JWT 并检查 Redis 会话状态
 *
 * 注意：
 * - 此 filter 仅在用户模块中使用
 * - 其他模块使用 shared-auth 模块的 JwtAuthFilter
 * - 通过 spring.auth.use-session-check=true 启用
 *
 * @author YaeSaraki
 */
@Component
@ConditionalOnProperty(name = ["spring.auth.use-session-check"], havingValue = "true", matchIfMissing = false)
class JwtUserAuthenticationTokenFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var userAuthService: UserAuthService

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = request.getHeader("MeoKey")

        // 没有 Token，继续执行
        if (token.isNullOrEmpty()) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            // 验证 Token 并检查会话状态
            val claims = userAuthService.authenticate(token)

            if (claims != null) {
                // Token 有效且会话存在，设置认证信息
                val authorities = if (claims.isAdminUser) {
                    AuthorityUtils.createAuthorityList("ROLE_ADMIN")
                } else {
                    AuthorityUtils.NO_AUTHORITIES
                }

                val authentication = UsernamePasswordAuthenticationToken(
                    claims.userId,
                    null,
                    authorities
                )

                // 将用户信息存储在 details 中
                authentication.details = """
                    {"userId":"${claims.userId}","username":"${claims.username}","isAdmin":${claims.isAdminUser}}
                """.trimIndent()

                SecurityContextHolder.getContext().authentication = authentication
                log.debug("User module JWT 验证成功: userId={}, isAdmin={}", claims.userId, claims.isAdminUser)
            }
        } catch (e: JwtException) {
            log.debug("JWT 验证失败: ${e.message}")
        } catch (e: Exception) {
            log.warn("JWT 认证过程发生异常: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }
}
