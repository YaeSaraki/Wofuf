package dev.saraki.wofuf.modules.forum.useCases.comments.downvoteComment

class DownvoteCommentDto {
    data class Request(
        val commentId: String,
        val userId: String,
    )

    data class Response(
        val success: Boolean = true,
        val newPoints: Int,
    )
}
