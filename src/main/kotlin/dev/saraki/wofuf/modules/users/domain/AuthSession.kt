package dev.saraki.wofuf.modules.users.domain

import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 00:32
 *   @description:
 */
data class AuthSession(
    val accessToken: JwtToken,
    val refreshToken: RefreshToken,
    val expiresIn: Long
): ValueObject<AuthSession>() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AuthSession
        return accessToken == other.accessToken && refreshToken == other.refreshToken && expiresIn == other.expiresIn
    }

    override fun hashCode(): Int {
        return accessToken.hashCode() * 31 + refreshToken.hashCode() * 31 + expiresIn.hashCode() * 31
    }

    override fun toString(): String {
        return "AuthSession(accessToken='$accessToken', refreshToken='$refreshToken', expiresIn=$expiresIn)"
    }
}
