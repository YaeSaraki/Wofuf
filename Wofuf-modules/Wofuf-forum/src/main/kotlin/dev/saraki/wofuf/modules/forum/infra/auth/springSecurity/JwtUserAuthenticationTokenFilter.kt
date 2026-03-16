package dev.saraki.wofuf.modules.forum.infra.auth.springSecurity

import dev.saraki.wofuf.modules.forum.services.auth.UserAuthService
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/23 21:00
 *   @description: JWT 认证过滤器
 *   当 Token 无效时不会抛出异常，而是继续执行请求（允许匿名访问公开资源）
 */

@Component("JwtUserAuthenticationTokenFilter")
class JwtUserAuthenticationTokenFilter: OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var userAuthService: UserAuthService

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain): Unit {
        val token = request.getHeader("MeoKey")
        
        // 没有 Token，继续执行（可能是匿名请求）
        if (token.isNullOrEmpty()) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            // 尝试验证 Token
            val claims = userAuthService.authenticate(token)
            
            if (claims != null) {
                // Token 有效，设置认证信息
                val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(
                    claims.username,
                    null,
                    AuthorityUtils.NO_AUTHORITIES
                )
                SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
            }
            // Token 无效但不抛出异常，允许继续访问公开资源
        } catch (e: JwtException) {
            log.debug("JWT 验证失败: ${e.message}")
            // 不抛出异常，允许继续访问公开资源
        } catch (e: Exception) {
            log.warn("JWT 认证过程发生异常: ${e.message}")
            // 不抛出异常，允许继续访问公开资源
        }

        filterChain.doFilter(request, response)
    }
}