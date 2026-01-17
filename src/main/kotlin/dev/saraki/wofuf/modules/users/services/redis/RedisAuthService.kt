package dev.saraki.wofuf.modules.users.services.redis

/**
 * Redis实现的认证服务
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/1/16 15:13
 * @description:
 */

import dev.saraki.wofuf.modules.users.domain.JwtClaims
import dev.saraki.wofuf.modules.users.domain.JwtToken
import dev.saraki.wofuf.modules.users.domain.RefreshToken
import dev.saraki.wofuf.modules.users.repos.persistence.UserEntity
import dev.saraki.wofuf.modules.users.services.IAuthService
import io.jsonwebtoken.Jwts
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Service
class RedisAuthService(
    @Autowired
    private val redisTemplate: RedisTemplate<String, Any>
) : IAuthService {

    // JWT签名密钥（使用新的API）
    private val secretKey: SecretKey = generateSecretKey()

    // Redis键前缀
    private val TOKEN_PREFIX = "auth:token:"
    private val REFRESH_TOKEN_PREFIX = "auth:refresh:"
    private val USER_TOKENS_PREFIX = "auth:user:"

    // 令牌过期时间（毫秒）
    private val TOKEN_EXPIRY = 1000 * 60 * 60 * 24 // 24小时
    private val REFRESH_TOKEN_EXPIRY = 1000 * 60 * 60 * 24 * 7 // 7天

    // 生成密钥的新方法
    private fun generateSecretKey(): SecretKey {
        // 生成足够长度的随机密钥（HS256需要至少256位/32字节）
        val keyBytes = ByteArray(32)
        val secureRandom = SecureRandom.getInstanceStrong()
        secureRandom.nextBytes(keyBytes)
        return SecretKeySpec(keyBytes, "HmacSHA256")
    }

    override fun signJWT(jwtClaims: JwtClaims): JwtToken {
        val now = Date()
        val expiryDate = Date(now.time + TOKEN_EXPIRY)

        return Jwts.builder()
            .subject(jwtClaims.username)
            .claim("userId", jwtClaims.userId)
            .claim("isEmailVerified", jwtClaims.isEmailVerified)
            .claim("email", jwtClaims.email)
            .claim("adminUser", jwtClaims.adminUser)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey)
            .compact()
    }

    override fun decodeJWT(jwtToken: JwtToken): JwtClaims? {
        try {
            val claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwtToken.removePrefix("Bearer "))
                .payload

            return JwtClaims(
                userId = claims["userId"] as String,
                isEmailVerified = claims["isEmailVerified"] as Boolean,
                email = claims["email"] as String,
                username = claims.subject,
                adminUser = claims["adminUser"] as Boolean
            )
        } catch (e: Exception) {
            return null
        }
    }

    override fun createRefreshToken(): RefreshToken {
        return UUID.randomUUID().toString()
    }

    override fun getTokens(username: String): List<JwtToken>? {
        val key = USER_TOKENS_PREFIX + username
        return redisTemplate.opsForList().range(key, 0, -1) as List<JwtToken>?
    }

    override fun saveAuthenticatedUser(user: UserEntity): Unit? {
        val username = user.username
        val userTokensKey = USER_TOKENS_PREFIX + username
        // 清空用户的所有令牌
        redisTemplate.opsForList().trim(userTokensKey, 0, 0)
        return null
    }

    override fun deAuthenticateUser(user: UserEntity): Unit? {
        val username = user.username
        val userTokensKey = USER_TOKENS_PREFIX + username

        // 获取用户的所有令牌
        val tokens = redisTemplate.opsForList().range(userTokensKey, 0, -1) as List<JwtToken>?

        // 删除所有令牌
        tokens?.forEach { token ->
            val tokenKey = TOKEN_PREFIX + token
            redisTemplate.delete(tokenKey)
        }

        // 删除用户令牌列表
        redisTemplate.delete(userTokensKey)

        // 删除刷新令牌
        val refreshTokenKey = REFRESH_TOKEN_PREFIX + username
        redisTemplate.delete(refreshTokenKey)

        return null
    }

    override fun refreshTokenExists(username: String): Result<Boolean>? {
        val key = REFRESH_TOKEN_PREFIX + username
        return Result.success(redisTemplate.hasKey(key))
    }

    override fun getUserNameFromRefreshToken(refreshToken: RefreshToken): String? {
        // 这里需要根据刷新令牌获取用户名
        // 可以考虑在存储刷新令牌时使用反向映射
        val keys = redisTemplate.keys("$REFRESH_TOKEN_PREFIX*")
        keys?.forEach { key ->
            val token = redisTemplate.opsForValue().get(key) as RefreshToken?
            if (token == refreshToken) {
                return key.removePrefix(REFRESH_TOKEN_PREFIX)
            }
        }
        return null
    }
}