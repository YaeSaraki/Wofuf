package dev.saraki.wofuf.modules.forum.useCases.admin.posts.hidePost

class HidePostDto {
    data class Request(
        val postId: String,
        val hiddenByMemberId: String,
    )
    data class Response(
        val postId: String,
        val status: String,
        val isHidden: Boolean = true,  // 兼容 CommentActionResponse 的 isHidden 命名
        val message: String = "Post hidden successfully"
    )
}
