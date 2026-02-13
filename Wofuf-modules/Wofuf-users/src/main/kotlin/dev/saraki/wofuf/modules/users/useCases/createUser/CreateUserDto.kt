package dev.saraki.wofuf.modules.users.useCases.createUser

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/16 17:26
 *   @description:
 */

class CreateUserDto {
    data class Request(
        val email: String,
        val username: String,
        val password: String
    )

    data class Response(
        val userId: String,
        val username: String,
        val email: String,
    )
}