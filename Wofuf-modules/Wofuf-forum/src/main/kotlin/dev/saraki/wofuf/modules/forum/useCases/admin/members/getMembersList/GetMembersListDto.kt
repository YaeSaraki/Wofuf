package dev.saraki.wofuf.modules.forum.useCases.admin.members.getMembersList

class GetMembersListDto {
    data class Request(
        val nickname: String? = null,
        val page: Int = 0,
        val size: Int = 20
    )

    data class Response(
        val members: List<MemberSummary>,
        val total: Long,
        val page: Int,
        val size: Int
    )

    data class MemberSummary(
        val memberId: String,
        val userId: String,
        val playerId: String,
        val nickname: String,
        val reputation: Int,
        val isBanned: Boolean,
        val permissions: List<String>
    )
}
