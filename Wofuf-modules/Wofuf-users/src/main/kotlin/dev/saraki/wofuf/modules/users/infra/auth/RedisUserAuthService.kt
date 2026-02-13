package dev.saraki.wofuf.modules.users.infra.auth

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 21:34
 *   @description:
 */

import dev.saraki.wofuf.auth.config.JwtConfig
import dev.saraki.wofuf.modules.users.domain.User
import dev.saraki.wofuf.modules.users.domain.valueObjects.*
import dev.saraki.wofuf.modules.users.services.auth.UserAuthRedisKeys
import dev.saraki.wofuf.modules.users.services.auth.UserAuthService
import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@Service("users-redisUserAuthService")
@Primary
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

    /* ---------------- 登录 ---------------- */

    override fun login(user: User): AuthSession? {
        val userId = user.userId.stringValue
        val username = user.username.value

        val jti = UUID.randomUUID().toString()
        val refreshToken = UUID.randomUUID().toString()

        val versionKey = UserAuthRedisKeys.tokenVersion(jwtConfig.tokenVersion)
        val tokenVersion =
            redis.opsForValue().get(versionKey) as? String ?: "0"

        val claims = JwtClaims.create(
            JwtClaimsProps(
                userId = userId,
                username = username,
                jti = jti,
                tokenVersion = tokenVersion
            )
        ).getOrThrow()

        val jwt = signJWT(claims)

        redis.opsForValue().set(
            UserAuthRedisKeys.session(jti),
            userId,
            jwtConfig.expiration,
            TimeUnit.MILLISECONDS
        )

        redis.opsForSet().add(
            UserAuthRedisKeys.userSessions(userId),
            jti
        )

        redis.opsForValue().set(
            UserAuthRedisKeys.refresh(refreshToken),
            jti,
            jwtConfig.refreshExpiration,
            TimeUnit.MILLISECONDS
        )

        return AuthSession.create(
            accessToken = jwt,
            refreshToken = refreshToken,
            expiresIn = jwtConfig.expiration
        ).getOrThrow()
    }

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

    /* ---------------- 刷新（Rotation） ---------------- */
    override fun refresh(refreshToken: RefreshToken): AuthSession? {
        val refreshKey = UserAuthRedisKeys.refresh(refreshToken)
        val jti = redis.opsForValue().get(refreshKey) as? String ?: return null

        val sessionKey = UserAuthRedisKeys.session(jti)
        val userId = redis.opsForValue().get(sessionKey) as? String ?: return null

        // 旧 refresh token 立即失效
        redis.delete(refreshKey)
        redis.delete(sessionKey)

        val newRefresh = UUID.randomUUID().toString()
        val newJti = UUID.randomUUID().toString()

        val versionKey = UserAuthRedisKeys.tokenVersion(userId)
        val tokenVersion = redis.opsForValue().get(versionKey) as? String ?: "0"

        val claims = JwtClaims.create(
            JwtClaimsProps(
                userId = userId,
                username = "", // username 不作为安全依据，可选
                jti = newJti,
                tokenVersion = tokenVersion
            )
        ).getOrThrow()

        val jwt = signJWT(claims)

        redis.opsForValue().set(
            UserAuthRedisKeys.session(newJti),
            userId,
            jwtConfig.expiration,
            TimeUnit.MILLISECONDS
        )

        redis.opsForValue().set(
            UserAuthRedisKeys.refresh(newRefresh),
            newJti,
            jwtConfig.refreshExpiration,
            TimeUnit.MILLISECONDS
        )

        log.info("刷新会话成功 userId={}, newJti={}", userId, newJti)

        return AuthSession.create(
            accessToken = jwt,
            refreshToken = newRefresh,
            expiresIn = jwtConfig.expiration
        ).getOrThrow()
    }

    /* ---------------- 注销 ---------------- */

    override fun logout(jwtToken: JwtToken) {
        decodeJWT(jwtToken)?.let {
            redis.delete(UserAuthRedisKeys.session(it.jti))
            log.info("会话注销 jti={}", it.jti)
        }
    }

    /* ---------------- 全端下线 ---------------- */

    override fun forceLogoutAll(userId: String) {
        redis.opsForValue().increment(
            UserAuthRedisKeys.tokenVersion(userId)
        )
        log.warn("用户被强制下线 userId={}", userId)
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
            null
        }
    }
}
