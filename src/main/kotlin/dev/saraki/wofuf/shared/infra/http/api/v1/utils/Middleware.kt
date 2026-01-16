package dev.saraki.wofuf.shared.infra.http.api.v1.utils

import dev.saraki.wofuf.AppConfig
import dev.saraki.wofuf.modules.users.services.IAuthService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 22:12
 *   @description:
 */

@Component
class Middleware (
    @Autowired
    private val authService: IAuthService,
    @Autowired
    private val appConfig: AppConfig
) {
    /**
     * 结束请求，返回指定状态码和消息
     */
    private fun endRequest(
        status: HttpStatus,
        message: String,
        response: HttpServletResponse
    ) {
        response.status = status.value()
        response.contentType = "application/json;charset=UTF-8"
        response.writer.write("{\"message\":\"$message\"}")
    }

    /**
     * 检查请求中是否包含有效令牌，如果有则将解码后的令牌存储在请求属性中
     */
    suspend fun includeDecodedTokenIfExists(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val token = request.getHeader("Authorization")

        if (token != null) {
            val decoded = authService.decodeJWT(token)
            val signatureFailed = decoded == null

            if (signatureFailed) {
                endRequest(HttpStatus.FORBIDDEN, "Token signature expired.", response)
                return false
            }

            // 检查令牌是否存在
            val username = decoded.username
            val tokens = authService.getTokens(username)

            // 无论令牌是否存在，都放行（保持原逻辑）
            if (tokens != null && tokens.isNotEmpty()) {
                request.setAttribute("decoded", decoded)
            }
            return true
        }
        return true // 无令牌直接放行
    }

    /**
     * 确保请求中包含有效令牌，如果没有则结束请求
     */
    suspend fun ensureAuthenticated(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {
        val token = request.getHeader("Authorization")

        if (token == null) {
            endRequest(HttpStatus.FORBIDDEN, "No access token provided", response)
            return false
        }

        val decoded = authService.decodeJWT(token)
        val signatureFailed = decoded == null

        if (signatureFailed) {
            endRequest(HttpStatus.FORBIDDEN, "Token signature expired.", response)
            return false
        }

        // 检查令牌是否有效
        val username = decoded.username
        val tokens = authService.getTokens(username)

        if (tokens != null && tokens.isNotEmpty()) {
            request.setAttribute("decoded", decoded)
            return true
        }

        endRequest(
            HttpStatus.FORBIDDEN,
            "Auth token not found. User is probably not logged in. Please login again.",
            response
        )
        return false
    }

    /**
     * 对应原 restrictedUrl 方法：
     * 生产环境下限制域名访问
     */
    fun restrictedUrl(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any
    ): Boolean {

        if (!appConfig.isProduction) {
            return true
        }

        val approvedDomainList = appConfig.approvedDomainList
        val domain = request.getHeader("Origin")

        val isValidDomain = approvedDomainList.contains(domain)
        println("Domain =$domain, valid?=$isValidDomain")

        return if (!isValidDomain) {
            response.status = HttpStatus.FORBIDDEN.value()
            response.writer.write("{\"message\":\"Unauthorized\"}")
            false
        } else {
            true
        }
    }


}