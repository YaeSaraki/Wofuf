package dev.saraki.wofuf.modules.forum.dtos

import dev.saraki.wofuf.modules.users.dtos.UserDto

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/7 15:57
 *   @description:
 */
data class MemberDto(
    val reputation: Int,
    val user: UserDto
)