package dev.saraki.wofuf.modules.forum.useCases.admin.posts.unfeaturePost

class UnfeaturePostDto {
    data class Request(
        val postId: String,
        val operatorMemberId: String,
    )
    data class Response(
        val postId: String,
        val isFeatured: Boolean,
        val message: String = "Post unfeatured successfully"
    )
}
