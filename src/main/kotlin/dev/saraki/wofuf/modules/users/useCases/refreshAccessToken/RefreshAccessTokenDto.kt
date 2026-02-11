package dev.saraki.wofuf.modules.users.useCases.refreshAccessToken

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 21:34
 *   @description:
 */

import dev.saraki.wofuf.modules.users.domain.JwtToken
import dev.saraki.wofuf.modules.users.domain.RefreshToken

class RefreshAccessTokenDto {
    data class Request(
        val id: String,
        val accessToken: JwtToken,
        val refreshToken: RefreshToken,
    )

    data class Response(
        val accessToken: JwtToken,
        val refreshToken: RefreshToken,
    )
}