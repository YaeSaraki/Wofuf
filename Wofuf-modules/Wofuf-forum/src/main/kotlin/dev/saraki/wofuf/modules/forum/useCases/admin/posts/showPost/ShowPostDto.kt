package dev.saraki.wofuf.modules.forum.useCases.admin.posts.showPost

class ShowPostDto {
    data class Request(
        val postId: String,
        val operatorMemberId: String,
    )
    data class Response(
        val postId: String,
        val status: String,
        val isHidden: Boolean = false,  // 兼容 CommentActionResponse 的 isHidden 命名
        val message: String = "Post shown successfully"
    )
}
