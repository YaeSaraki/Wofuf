package dev.saraki.wofuf.auth.infra

import dev.saraki.wofuf.auth.config.JwtConfig
import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

/**
 * JWT 工具类
 * 统一的 JWT 验证和解析
 *
 * 职责：
 * - 验证 JWT 签名
 * - 提取 JWT 中的基本信息（userId, isAdmin, username）
 *
 * 注意：不查询 Redis，不验证会话状态
 * 会话验证由用户模块负责，下游服务只验证签名
 *
 * @author YaeSaraki
 */
@Component
class JwtUtils(private val jwtConfig: JwtConfig) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val secretKey: SecretKey = SecretKeySpec(
        jwtConfig.secret.toByteArray(),
        "HmacSHA256"
    )

    /**
     * 验证 JWT 并提取基本信息
     *
     * @param token JWT Token
     * @return JwtUserInfo 如果 Token 有效，否则 null
     */
    fun verifyToken(token: String): JwtUserInfo? {
        return try {
            val clean = token.removePrefix("Bearer ").trim()
            val payload = Jwts.parser()
                .verifyWith(secretKey)
                .clockSkewSeconds(jwtConfig.clockSkew.toLong())
                .build()
                .parseSignedClaims(clean)
                .payload

            JwtUserInfo(
                userId = payload["uid"] as String,
                username = payload.subject ?: "",
                isAdmin = payload["admin"] as? Boolean ?: false
            )
        } catch (e: Exception) {
            log.debug("JWT 验证失败: ${e.message}")
            null
        }
    }

    /**
     * 解码 JWT（不验证签名，仅用于调试）
     */
    fun decodeToken(token: String): JwtUserInfo? {
        return try {
            val clean = token.removePrefix("Bearer ").trim()
            val payload = Jwts.parser()
                .build()
                .parseSignedClaims(clean)
                .payload

            JwtUserInfo(
                userId = payload["uid"] as String,
                username = payload.subject ?: "",
                isAdmin = payload["admin"] as? Boolean ?: false
            )
        } catch (e: Exception) {
            log.debug("JWT 解码失败: ${e.message}")
            null
        }
    }
}

/**
 * JWT 用户信息
 * 只包含最基本的验证信息
 */
data class JwtUserInfo(
    val userId: String,
    val username: String,
    val isAdmin: Boolean
) {
    /**
     * 将用户信息转换为 JSON 字符串
     * 用于存储在 authentication.details 中
     */
    fun toJson(): String {
        return """
            {"userId":"$userId","username":"$username","isAdmin":$isAdmin}
        """.trimIndent()
    }

    companion object {
        /**
         * 从 JSON 字符串解析用户信息
         */
        fun fromJson(json: String): JwtUserInfo? {
            return try {
                // 简单的 JSON 解析
                val userId = json.substringAfter("\"userId\":\"").substringBefore("\"")
                val username = json.substringAfter("\"username\":\"").substringBefore("\"")
                val isAdmin = json.substringAfter("\"isAdmin\":").substringBefore("}").toBoolean()
                JwtUserInfo(userId, username, isAdmin)
            } catch (e: Exception) {
                null
            }
        }
    }
}
