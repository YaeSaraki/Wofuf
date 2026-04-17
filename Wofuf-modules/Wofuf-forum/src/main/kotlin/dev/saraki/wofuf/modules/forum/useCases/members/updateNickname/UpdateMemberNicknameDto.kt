package dev.saraki.wofuf.modules.forum.useCases.members.updateNickname

class UpdateMemberNicknameDto {
    data class Request(
        val memberId: String,
        val newNickname: String
    )

    data class Response(
        val success: Boolean,
        val message: String? = null
    )
}
