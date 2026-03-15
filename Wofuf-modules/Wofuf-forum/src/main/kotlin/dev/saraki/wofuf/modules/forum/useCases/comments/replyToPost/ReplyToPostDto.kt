package dev.saraki.wofuf.modules.forum.useCases.comments.replyToPost

class ReplyToPostDto {
    data class Request(
        val postId: String,
        val userId: String,
        val comment: String,
    )

    data class Response(
        val success: Boolean = true
    )
}
