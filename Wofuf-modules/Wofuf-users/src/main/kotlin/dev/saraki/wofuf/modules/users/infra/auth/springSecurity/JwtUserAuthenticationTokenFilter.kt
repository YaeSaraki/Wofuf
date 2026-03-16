package dev.saraki.wofuf.modules.users.infra.auth.springSecurity

import dev.saraki.wofuf.modules.users.services.auth.UserAuthService
import io.jsonwebtoken.JwtException
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
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
 *   @description:
 */

@Component
class JwtUserAuthenticationTokenFilter: OncePerRequestFilter() {

    @Autowired
    private lateinit var userAuthService: UserAuthService

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain): Unit {
        val token = request.getHeader("MeoKey")
        if (token.isNullOrEmpty()) {
            filterChain.doFilter(request, response)
            return
        }

        val claims = userAuthService.authenticate(token) ?: throw JwtException("JWT 令牌无效喵，用户验证系统爆炸了喵")
        val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(
            claims.username,
            null,
            AuthorityUtils.NO_AUTHORITIES
        )

        SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
        filterChain.doFilter(request, response)
        return
    }
}