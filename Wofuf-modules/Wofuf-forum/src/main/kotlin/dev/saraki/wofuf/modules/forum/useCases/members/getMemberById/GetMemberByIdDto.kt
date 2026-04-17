package dev.saraki.wofuf.modules.forum.useCases.members.getMemberById

/**
 * @author YaeSaraki
 * @date 2026/4/16
 * @description Data transfer objects for getting a member by ID
 */
class GetMemberByIdDto {
    data class Request(
        val memberId: String
    )

    data class Response(
        val memberId: String,
        val nickname: String
    )
}