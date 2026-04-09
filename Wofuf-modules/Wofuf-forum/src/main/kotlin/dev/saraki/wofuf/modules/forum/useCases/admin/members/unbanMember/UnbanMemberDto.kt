package dev.saraki.wofuf.modules.forum.useCases.admin.members.unbanMember

class UnbanMemberDto {
    data class Request(val memberId: String)
    data class Response(
        val memberId: String,
        val isBanned: Boolean,
        val message: String = "Member unbanned successfully"
    )
}
