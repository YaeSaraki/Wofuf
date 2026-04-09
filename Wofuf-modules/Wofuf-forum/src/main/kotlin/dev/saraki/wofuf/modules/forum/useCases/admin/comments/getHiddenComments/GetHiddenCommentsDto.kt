package dev.saraki.wofuf.modules.forum.useCases.admin.comments.getHiddenComments

class GetHiddenCommentsDto {
    data class Request(
        val page: Int = 0,
        val size: Int = 20
    )

    data class Response(
        val comments: List<CommentSummary>,
        val total: Long,
        val page: Int,
        val size: Int
    )

    data class CommentSummary(
        val commentId: String,
        val postId: String,
        val content: String,
        val isHidden: Boolean,
        val hiddenAt: Long?,
        val hiddenBy: String?,
        val authorId: String
    )
}
