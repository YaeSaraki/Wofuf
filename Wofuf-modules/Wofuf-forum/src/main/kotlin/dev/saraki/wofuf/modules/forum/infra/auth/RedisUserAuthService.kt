package dev.saraki.wofuf.modules.forum.infra.auth

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 21:34
 *   @description:
 */

import dev.saraki.wofuf.auth.config.JwtConfig
import dev.saraki.wofuf.modules.forum.services.auth.UserAuthService

import dev.saraki.wofuf.modules.users.domain.valueObjects.*
import dev.saraki.wofuf.modules.users.services.auth.UserAuthRedisKeys

import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Service("forum-redisUserAuthService")
class RedisUserAuthService(
    @Qualifier("AuthRedisTemplate")
    private val redis: RedisTemplate<String, Any>,
    private val jwtConfig: JwtConfig,
) : UserAuthService {
    private val log = LoggerFactory.getLogger(javaClass)

    private val secretKey: SecretKey = SecretKeySpec(
        jwtConfig.secret.toByteArray(),
        "HmacSHA256"
    )

    /* ---------------- 鉴权 ---------------- */
    override fun authenticate(jwtToken: JwtToken): JwtClaims? {
        val claims = decodeJWT(jwtToken) ?: return null

        val sessionKey = UserAuthRedisKeys.session(claims.jti)
        if (!redis.hasKey(sessionKey)) return null

        val versionKey = UserAuthRedisKeys.tokenVersion(claims.tokenVersion)
        val currentVersion =
            redis.opsForValue().get(versionKey) as? String ?: "0"

        if (claims.tokenVersion != currentVersion) return null

        return claims
    }

    /* ---------------- JWT 基础能力 ---------------- */

     override fun signJWT(jwtClaims: JwtClaims): JwtToken {
        val now = Date()
        val exp = Date(now.time + jwtConfig.expiration)

        return Jwts.builder()
            .subject(jwtClaims.username)
            .claim("uid", jwtClaims.userId)
            .claim("jti", jwtClaims.jti)
            .claim("ver", jwtClaims.tokenVersion)
            .issuedAt(now)
            .expiration(exp)
            .signWith(secretKey)
            .compact()
    }

    override fun decodeJWT(jwtToken: JwtToken): JwtClaims? {
        return try {
            val clean = jwtToken.removePrefix("Bearer ").trim()
            
            val payload = Jwts.parser()
                .verifyWith(secretKey)
                .clockSkewSeconds(jwtConfig.clockSkew.toLong())
                .build()
                .parseSignedClaims(clean)
                .payload

            JwtClaims.create(
                JwtClaimsProps(
                    userId = payload["uid"] as String,
                    username = payload.subject ?: "",
                    jti = payload["jti"] as String,
                    tokenVersion = payload["ver"] as String
                )
            ).getOrNull()
        } catch (e: Exception) {
            log.debug("JWT 解码失败: ${e.message}")
            null
        }
    }
}
