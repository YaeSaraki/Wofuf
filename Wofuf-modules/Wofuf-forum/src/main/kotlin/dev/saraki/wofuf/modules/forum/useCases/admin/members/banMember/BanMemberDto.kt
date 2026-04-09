package dev.saraki.wofuf.modules.forum.useCases.admin.members.banMember

class BanMemberDto {
    data class Request(
        val memberId: String,
        val bannedByMemberId: String,
        val reason: String? = null,
        val bannedUntilMinutes: Int? = null
    )
    data class Response(
        val memberId: String,
        val isBanned: Boolean,
        val bannedUntil: String?,
        val message: String = "Member banned successfully"
    )
}
