package dev.saraki.wofuf.modules.forum.useCases.admin.posts.getPostsForReview

class GetPostsForReviewDto {
    data class Request(
        val page: Int = 0,
        val size: Int = 20
    )

    data class Response(
        val posts: List<PostSummary>,
        val total: Long,
        val page: Int,
        val size: Int
    )

    data class PostSummary(
        val postId: String,
        val title: String,
        val status: String,
        val dateTimePosted: Long,
        val authorId: String
    )
}
