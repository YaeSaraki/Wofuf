package dev.saraki.wofuf.modules.forum.infra.security

import dev.saraki.wofuf.auth.infra.JwtAuthFilter
import dev.saraki.wofuf.modules.forum.infra.events.MemberLogoutEventHandler
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Forum 模块 JWT 认证过滤器
 * 在 shared-auth 的 JwtAuthFilter 之后执行，额外检查 JTI 黑名单
 *
 * 注意：不再自己解析 JWT 和设置 SecurityContext
 * SecurityContext 由 JwtAuthFilter 统一设置，这里只做黑名单检查
 *
 * @author YaeSaraki
 * @date 2026/4/24
 */
@Component
class ForumJwtAuthFilter(
    private val logoutEventHandler: MemberLogoutEventHandler
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain
    ) {
        // 获取 JwtAuthFilter 已设置的认证信息
        val userInfo = JwtAuthFilter.getCurrentUser() ?: run {
            chain.doFilter(request, response)
            return
        }

        // 如果用户已登录且有 JTI，检查黑名单
        val jti = userInfo.jti
        if (jti != null && logoutEventHandler.isBlacklisted(userInfo.userId, jti)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been invalidated")
            return
        }

        chain.doFilter(request, response)
    }
}