package dev.saraki.wofuf.modules.users.useCases.login

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 11:07
 *   @description:
 */

import dev.saraki.wofuf.modules.users.domain.JwtToken
import dev.saraki.wofuf.modules.users.domain.RefreshToken

class LoginDto {
    data class LoginRequest(
        val username: String,
        val password: String,
    )

    data class LoginResponse(
        val accessToken: JwtToken,
        val refreshToken: RefreshToken,
    )
}