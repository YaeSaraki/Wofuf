package dev.saraki.wofuf.modules.forum.mappers

import dev.saraki.wofuf.modules.forum.domain.MemberDetails
import dev.saraki.wofuf.modules.forum.dtos.MemberDto
import dev.saraki.wofuf.modules.users.dtos.UserDto

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 13:30
 *   @description:
 */
object MemberDetailsMap {
    fun from(memberDetails: MemberDetails): MemberDto {
        return MemberDto(
            reputation = memberDetails.reputation,
            user = UserDto(
                userName = memberDetails.nickName.value,
                isEmailVerified = memberDetails.isEmailVerified ?: false,
                isAdminUser = memberDetails.isAdminUser,
                isDeleted = memberDetails.isDeleted
            )
        )
    }
}