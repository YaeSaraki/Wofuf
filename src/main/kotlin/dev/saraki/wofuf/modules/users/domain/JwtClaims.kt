package dev.saraki.wofuf.modules.users.domain

import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:29
 *   @description: JWT 令牌值对象
 */
data class JwtClaims(
    val userId: String,
    val username: String,

    /**
     * JWT 唯一 ID，用于：
     * - Redis 校验
     * - 精准注销
     * - 防重放
     */
    val jti: String,

    /**
     * 会话版本号
     * - 用户改密码 / 封号 / 强制下线时递增
     */
    val tokenVersion: Long
): ValueObject<JwtClaims>() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as JwtClaims
        return userId == other.userId && username == other.username && jti == other.jti && tokenVersion == other.tokenVersion
    }

    override fun hashCode(): Int {
        return userId.hashCode() * 31 + username.hashCode() * 31 + jti.hashCode() * 31 + tokenVersion.hashCode() * 31
    }

    override fun toString(): String {
        return "JwtClaims(userId='$userId', username='$username', jti='$jti', tokenVersion=$tokenVersion)"
    }
}


typealias JwtToken = String
typealias RefreshToken = String