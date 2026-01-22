package dev.saraki.wofuf.modules.users.infra.auth

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 21:34
 *   @description:
 */

import dev.saraki.wofuf.modules.users.domain.AuthSession
import dev.saraki.wofuf.modules.users.domain.JwtClaims
import dev.saraki.wofuf.modules.users.domain.JwtToken
import dev.saraki.wofuf.modules.users.domain.RefreshToken
import dev.saraki.wofuf.modules.users.domain.User
import dev.saraki.wofuf.modules.users.services.auth.AuthProperties
import dev.saraki.wofuf.modules.users.services.auth.AuthRedisKeys
import dev.saraki.wofuf.modules.users.services.auth.IAuth
import io.jsonwebtoken.Jwts
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
@Service
@Primary
class RedisAuth(
    private val redis: RedisTemplate<String, Any>,
    private val props: AuthProperties
) : IAuth {

    private val log = LoggerFactory.getLogger(javaClass)

    private val secretKey: SecretKey by lazy {
        SecretKeySpec(
            Base64.getDecoder().decode(props.secret),
            "HmacSHA256"
        )
    }

    /* ---------------- 登录 ---------------- */

    override fun login(user: User): AuthSession {
        val userId = user.id.value.id
        val username = user.userProps.username.value

        val jti = UUID.randomUUID().toString()
        val refreshToken = UUID.randomUUID().toString()

        val versionKey = AuthRedisKeys.tokenVersion(userId)
        val tokenVersion =
            redis.opsForValue().get(versionKey) as? Long ?: 0L

        val claims = JwtClaims(
            userId = userId,
            username = username,
            jti = jti,
            tokenVersion = tokenVersion
        )

        val jwt = signJWT(claims)

        redis.opsForValue().set(
            AuthRedisKeys.session(jti),
            userId,
            props.accessExpiryMs,
            TimeUnit.MILLISECONDS
        )

        redis.opsForSet().add(
            AuthRedisKeys.userSessions(userId),
            jti
        )

        redis.opsForValue().set(
            AuthRedisKeys.refresh(refreshToken),
            jti,
            props.refreshExpiryMs,
            TimeUnit.MILLISECONDS
        )


        log.info("用户登录成功 userId={}, jti={}", userId, jti)

        return AuthSession(
            accessToken = jwt,
            refreshToken = refreshToken,
            expiresIn = props.accessExpiryMs
        )
    }

    /* ---------------- 鉴权 ---------------- */

    override fun authenticate(jwtToken: JwtToken): JwtClaims? {
        val claims = decodeJWT(jwtToken) ?: return null

        val sessionKey = AuthRedisKeys.session(claims.jti)
        if (redis.hasKey(sessionKey) != true) return null

        val versionKey = AuthRedisKeys.tokenVersion(claims.userId)
        val currentVersion =
            redis.opsForValue().get(versionKey) as? Long ?: 0L

        if (claims.tokenVersion != currentVersion) return null

        return claims
    }

    /* ---------------- 刷新（Rotation） ---------------- */

    override fun refresh(refreshToken: RefreshToken): AuthSession? {
        val refreshKey = AuthRedisKeys.refresh(refreshToken)
        val jti = redis.opsForValue().get(refreshKey) as? String ?: return null

        val sessionKey = AuthRedisKeys.session(jti)
        val userId = redis.opsForValue().get(sessionKey) as? String ?: return null

        // 旧 refresh token 立即失效
        redis.delete(refreshKey)
        redis.delete(sessionKey)

        val newRefresh = UUID.randomUUID().toString()
        val newJti = UUID.randomUUID().toString()

        val versionKey = AuthRedisKeys.tokenVersion(userId)
        val tokenVersion =
            redis.opsForValue().get(versionKey) as? Long ?: 0L

        val claims = JwtClaims(
            userId = userId,
            username = "", // username 不作为安全依据，可选
            jti = newJti,
            tokenVersion = tokenVersion
        )

        val jwt = signJWT(claims)

        redis.opsForValue().set(
            AuthRedisKeys.session(newJti),
            userId,
            props.accessExpiryMs,
            TimeUnit.MILLISECONDS
        )

        redis.opsForValue().set(
            AuthRedisKeys.refresh(newRefresh),
            newJti,
            props.refreshExpiryMs,
            TimeUnit.MILLISECONDS
        )

        log.info("刷新会话成功 userId={}, newJti={}", userId, newJti)

        return AuthSession(jwt, newRefresh, props.accessExpiryMs)
    }

    /* ---------------- 注销 ---------------- */

    override fun logout(jwtToken: JwtToken) {
        decodeJWT(jwtToken)?.let {
            redis.delete(AuthRedisKeys.session(it.jti))
            log.info("会话注销 jti={}", it.jti)
        }
    }

    /* ---------------- 全端下线 ---------------- */

    override fun forceLogoutAll(userId: String) {
        redis.opsForValue().increment(
            AuthRedisKeys.tokenVersion(userId)
        )
        log.warn("用户被强制下线 userId={}", userId)
    }

    /* ---------------- JWT 基础能力 ---------------- */

    private fun signJWT(jwtClaims: JwtClaims): JwtToken {
        val now = Date()
        val exp = Date(now.time + props.accessExpiryMs)

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

    private fun decodeJWT(jwtToken: JwtToken): JwtClaims? {
        return try {
            val clean = jwtToken.removePrefix("Bearer ").trim()
            val payload = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(clean)
                .payload

            JwtClaims(
                userId = payload["uid"] as String,
                username = payload.subject ?: "",
                jti = payload["jti"] as String,
                tokenVersion = (payload["ver"] as Number).toLong()
            )
        } catch (e: Exception) {
            null
        }
    }
}
