package dev.saraki.wofuf.modules.users.domain

import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:29
 *   @description: JWT 令牌值对象
 */

data class JwtClaimsProps(
    val userId: String,
    val username: String,
    val jti: String,
    val tokenVersion: Long
)

class JwtClaims private constructor(
    props: JwtClaimsProps
): ValueObject<JwtClaimsProps>(props) {
    val userId: String get() = props.userId
    val username: String get() = props.username
    val jti: String get() = props.jti
    val tokenVersion: Long get() = props.tokenVersion
    companion object {
        fun create(props: JwtClaimsProps): Result<JwtClaims> {
            return Result.success(JwtClaims(props))
        }
    }
}

typealias JwtToken = String
typealias RefreshToken = String