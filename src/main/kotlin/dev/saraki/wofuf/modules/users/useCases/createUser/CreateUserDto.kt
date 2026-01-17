package dev.saraki.wofuf.modules.users.useCases.createUser

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/16 17:26
 *   @description:
 */
data class CreateUserDto(
    val email: String,
    val username: String,
    val password: String
)