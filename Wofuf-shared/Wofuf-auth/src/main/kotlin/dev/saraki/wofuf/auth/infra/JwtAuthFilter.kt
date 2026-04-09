package dev.saraki.wofuf.auth.infra

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
 * JWT 认证过滤器
 * 统一的 JWT 认证入口
 *
 * 职责：
 * - 从请求头获取 JWT Token
 * - 验证 JWT 签名
 * - 设置基本的认证信息
 *
 * 注意：
 * - 不查询 Redis，不验证会话状态
 * - 只设置基本的用户认证信息
 * - 权限控制由各模块自己管理
 *
 * @author YaeSaraki
 */
@Component
class JwtAuthFilter : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(javaClass)

    @Autowired
    private lateinit var jwtUtils: JwtUtils

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = request.getHeader("MeoKey")

        // 没有 Token，继续执行（可能是匿名请求）
        if (token.isNullOrEmpty()) {
            filterChain.doFilter(request, response)
            return
        }

        try {
            // 验证 JWT Token
            val userInfo = jwtUtils.verifyToken(token)

            if (userInfo != null) {
                // Token 有效，设置认证信息
                val authorities = if (userInfo.isAdmin) {
                    AuthorityUtils.createAuthorityList("ROLE_ADMIN")
                } else {
                    AuthorityUtils.NO_AUTHORITIES
                }

                val authentication = UsernamePasswordAuthenticationToken(
                    userInfo.userId,  // principal: userId
                    null,             // credentials: null
                    authorities       // authorities: 角色列表
                )

                // 将用户信息存储在 details 中（JSON 格式）
                authentication.details = userInfo.toJson()

                SecurityContextHolder.getContext().authentication = authentication
                log.debug("JWT 验证成功: userId={}, isAdmin={}", userInfo.userId, userInfo.isAdmin)
            } else {
                log.debug("JWT 验证失败: token 无效或已过期")
            }
        } catch (e: Exception) {
            log.warn("JWT 认证过程发生异常: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }

    companion object {
        /**
         * 获取当前用户 ID
         */
        fun getCurrentUserId(): String? {
            val authentication = SecurityContextHolder.getContext().authentication
            return authentication?.principal?.toString()
        }

        /**
         * 获取当前用户信息
         */
        fun getCurrentUser(): JwtUserInfo? {
            val authentication = SecurityContextHolder.getContext().authentication
            val detailsJson = authentication?.details as? String ?: return null
            return JwtUserInfo.fromJson(detailsJson)
        }

        /**
         * 检查当前用户是否为管理员
         */
        fun isAdmin(): Boolean {
            val authentication = SecurityContextHolder.getContext().authentication
            return authentication?.authorities?.any {
                it.authority == "ROLE_ADMIN"
            } == true
        }

        /**
         * 检查当前用户是否已登录
         */
        fun isAuthenticated(): Boolean {
            val authentication = SecurityContextHolder.getContext().authentication
            return authentication != null && authentication.isAuthenticated && authentication.principal != null
        }
    }
}
