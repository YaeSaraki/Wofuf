package dev.saraki.wofuf.modules.forum.useCases.admin.posts.unpinPost

class UnpinPostDto {
    data class Request(
        val postId: String,
        val operatorMemberId: String,
    )
    data class Response(
        val postId: String,
        val isPinned: Boolean,
        val message: String = "Post unpinned successfully"
    )
}
