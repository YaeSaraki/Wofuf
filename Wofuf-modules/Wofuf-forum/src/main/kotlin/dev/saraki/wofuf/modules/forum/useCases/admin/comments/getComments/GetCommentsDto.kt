package dev.saraki.wofuf.modules.forum.useCases.admin.comments.getComments

class GetCommentsDto {
    data class Request(
        val page: Int = 0,
        val size: Int = 20,
        val search: String? = null,  // 按作者昵称搜索
        val contentSearch: String? = null,  // 按评论内容搜索
        val includeHidden: Boolean = false  // 是否包含隐藏评论
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
        val postSlug: String,
        val content: String,
        val isHidden: Boolean,
        val hiddenAt: Long?,
        val hiddenBy: String?,
        val authorId: String,
        val authorNickname: String,
        val createdAt: Long
    )
}
