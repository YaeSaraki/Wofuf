package dev.saraki.wofuf.modules.forum.useCases.comments.upvoteComment

class UpvoteCommentDto {
    data class Request(
        val commentId: String,
        val userId: String,
    )

    data class Response(
        val success: Boolean = true,
        val newPoints: Int,
    )
}
