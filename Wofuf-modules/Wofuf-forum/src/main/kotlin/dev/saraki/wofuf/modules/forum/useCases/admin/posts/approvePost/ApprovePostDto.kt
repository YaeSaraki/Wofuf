package dev.saraki.wofuf.modules.forum.useCases.admin.posts.approvePost

class ApprovePostDto {
    data class Request(val postId: String)
    data class Response(
        val postId: String,
        val status: String,
        val message: String = "Post approved successfully"
    )
}
