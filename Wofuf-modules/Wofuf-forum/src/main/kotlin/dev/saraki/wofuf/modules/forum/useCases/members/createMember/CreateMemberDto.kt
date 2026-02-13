package dev.saraki.wofuf.modules.forum.useCases.members.createMember

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/11 17:55
 *   @description:
 */
class CreateMemberDto {
    data class Request(
        val userId: String,
        val playerId: String,
        val nickName: String,
        val lastPlayed: String,
        val code: String
    )
}