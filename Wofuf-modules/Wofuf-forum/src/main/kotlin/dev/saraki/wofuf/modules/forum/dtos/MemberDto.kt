package dev.saraki.wofuf.modules.forum.dtos

import dev.saraki.wofuf.modules.users.dtos.UserDto

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/7 15:57
 *   @description:
 */
data class MemberDto(
    val nickname: String,
    val reputation: Int,
    val playerSkin: String?,  // 玩家头像皮肤 (base64)
)