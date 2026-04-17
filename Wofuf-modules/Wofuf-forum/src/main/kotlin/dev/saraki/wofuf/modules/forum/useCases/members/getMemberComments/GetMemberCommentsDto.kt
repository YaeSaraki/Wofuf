package dev.saraki.wofuf.modules.forum.useCases.members.getMemberComments

class GetMemberCommentsDto {
    data class Request(
        val memberId: String,
        val page: Int = 0,
        val size: Int = 10
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
        val postTitle: String,
        val content: String,
        val createdAt: String,
        val points: Int
    )
}
