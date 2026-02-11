package dev.saraki.wofuf.modules.users.domain

import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 00:32
 *   @description:
 */
data class AuthSessionProps(
    val accessToken: JwtToken,
    val refreshToken: RefreshToken,
    val expiresIn: Long
)

class AuthSession private constructor(
    props: AuthSessionProps
) : ValueObject<AuthSessionProps>(props) {

    val accessToken: JwtToken
        get() = props.accessToken
    val refreshToken: RefreshToken
        get() = props.refreshToken
    val expiresIn: Long
        get() = props.expiresIn

    companion object {
        fun create(
            accessToken: JwtToken,
            refreshToken: RefreshToken,
            expiresIn: Long
        ): Result<AuthSession> {
            return Result.success(AuthSession(AuthSessionProps(accessToken, refreshToken, expiresIn)))
        }
    }
}
