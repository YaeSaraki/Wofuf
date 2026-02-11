package dev.saraki.wofuf.modules.users.useCases.getUserByUsername

import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/17 21:53
 *   @description:
 */

class GetUserByUsernameDto {
    data class Request(
        val username: String,
    )

    data class Response(
        val username: String,
        val email: String,
        val isEmailVerified: Boolean,
        val adminUser: Boolean,
        val lastLogin: LocalDateTime,
    )
}
