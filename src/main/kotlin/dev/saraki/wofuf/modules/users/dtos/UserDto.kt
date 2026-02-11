package dev.saraki.wofuf.modules.users.dtos

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 23:17
 *   @description:
 */


data class UserDto(
    val userName: String,
    val isEmailVerified: Boolean,
    val isAdminUser: Boolean?,
    val isDeleted: Boolean?,
)