package dev.saraki.wofuf.modules.users.useCases.refreshAccessToken

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 21:34
 *   @description: 刷新访问令牌 DTO
 *   注意：刷新令牌时只需要 refreshToken，不需要 accessToken
 */

import dev.saraki.wofuf.modules.users.domain.valueObjects.JwtToken
import dev.saraki.wofuf.modules.users.domain.valueObjects.RefreshToken

class RefreshAccessTokenDto {
    data class Request(
        val refreshToken: RefreshToken,
    )

    data class Response(
        val userId: String,
        val accessToken: JwtToken,
        val refreshToken: RefreshToken,
    )
}