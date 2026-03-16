package dev.saraki.wofuf.modules.forum.useCases.comments.replyToPost

class ReplyToPostDto {
    data class Request(
        val postSlug: String?,  // 帖子 slug（与 postId 二选一）
        val postId: String?,    // 帖子 UUID（与 postSlug 二选一）
        val userId: String,
        val comment: String,
    )

    data class Response(
        val success: Boolean = true
    )
}
