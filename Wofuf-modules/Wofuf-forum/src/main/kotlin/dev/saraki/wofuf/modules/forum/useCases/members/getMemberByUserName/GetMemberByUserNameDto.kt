package dev.saraki.wofuf.modules.forum.useCases.members.getMemberByUserName

class GetMemberByUserNameDto {
    data class Request(val username: String)

    data class Response(
        val memberId: String,
        val userId: String,
        val playerId: String,
        val nickname: String,
        val reputation: Int,
    )
}
