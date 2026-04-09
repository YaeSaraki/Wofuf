package dev.saraki.wofuf.modules.forum.useCases.admin.posts.hidePost

class HidePostDto {
    data class Request(
        val postId: String,
        val hiddenByMemberId: String
    )
    data class Response(
        val postId: String,
        val status: String,
        val message: String = "Post hidden successfully"
    )
}
