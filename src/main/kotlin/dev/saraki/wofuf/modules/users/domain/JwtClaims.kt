package dev.saraki.wofuf.modules.users.domain

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:29
 *   @description: JWT 令牌值对象
 */
data class JwtClaims(
    val userId: String,
    val isEmailVerified: Boolean,
    val email: String,
    val username: String,
    val adminUser: Boolean,
)

typealias JwtToken = String
typealias RefreshToken = String