package dev.saraki.wofuf.modules.users.useCases.logout

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 21:34
 *   @description:
 */

import dev.saraki.wofuf.modules.users.domain.JwtToken

class LogoutDto {
    data class LogoutRequest(
        val id: String,
        val accessToken: JwtToken,
    )

    data class LogoutResponse(
        val message: String,
    )
}