package dev.saraki.wofuf.modules.forum.useCases.admin.posts.showPost

class ShowPostDto {
    data class Request(val postId: String)
    data class Response(
        val postId: String,
        val status: String,
        val message: String = "Post shown successfully"
    )
}
