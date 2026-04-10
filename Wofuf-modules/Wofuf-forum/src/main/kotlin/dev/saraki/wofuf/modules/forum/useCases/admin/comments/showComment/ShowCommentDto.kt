package dev.saraki.wofuf.modules.forum.useCases.admin.comments.showComment

class ShowCommentDto {
    data class Request(
        val commentId: String,
        val userId: String  // 当前操作用户的 ID
    )
    data class Response(
        val commentId: String,
        val isHidden: Boolean,
        val message: String = "Operation successful"
    )
}
