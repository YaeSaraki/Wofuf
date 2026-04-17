package dev.saraki.wofuf.modules.forum.useCases.members.deleteOwnComment

class DeleteOwnCommentDto {
    data class Request(
        val commentId: String,
        val currentUserId: String
    )

    data class Response(
        val success: Boolean,
        val message: String? = null
    )
}
