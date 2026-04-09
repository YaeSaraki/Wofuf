package dev.saraki.wofuf.modules.forum.useCases.members.getCurrentMember

class GetCurrentMemberDto {
    data class Request(val userId: String)

    data class Response(
        val memberId: String,
        val userId: String,
        val playerId: String,
        val nickname: String,
        val reputation: Int,
        val permissions: List<String>,
        val isAdminUser: Boolean,
        val isBanned: Boolean,
        val bannedAt: Long?,
        val bannedUntil: Long?,
        val bannedReason: String?,
    )
}
