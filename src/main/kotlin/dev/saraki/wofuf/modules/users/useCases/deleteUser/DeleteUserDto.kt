package dev.saraki.wofuf.modules.users.useCases.deleteUser

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/17 16:31
 *   @description:
 */
class DeleteUserDto{
    data class Request(
        val userId: String,
        val accessToken: String,
    )
}
