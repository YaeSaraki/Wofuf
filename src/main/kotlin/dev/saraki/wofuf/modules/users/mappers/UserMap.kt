package dev.saraki.wofuf.modules.users.mappers

import dev.saraki.wofuf.modules.users.domain.User
import dev.saraki.wofuf.modules.users.dtos.UserDto

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/17 00:55
 *   @description:
 */
object UserMap {
    fun from(user: User): UserDto {
        return UserDto(
            userName = user.username.value,
            isEmailVerified = user.isEmailVerified,
            isAdminUser = user.isAdminUser,
            isDeleted = user.isDeleted,
        )
    }
}