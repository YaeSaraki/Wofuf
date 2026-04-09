package dev.saraki.wofuf.modules.forum.useCases.admin.members.getBannedMembers

class GetBannedMembersDto {
    data class Request(
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
        val nickname: String,
        val isBanned: Boolean,
        val bannedAt: Long?,
        val bannedUntil: Long?,
        val bannedReason: String?,
        val bannedBy: String?
    )
}
