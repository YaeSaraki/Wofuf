package dev.saraki.wofuf.modules.forum.useCases.admin.posts.setPostUnderReview

class SetPostUnderReviewDto {
    data class Request(val postId: String)
    data class Response(
        val postId: String,
        val status: String,
        val message: String = "Post set under review successfully"
    )
}
