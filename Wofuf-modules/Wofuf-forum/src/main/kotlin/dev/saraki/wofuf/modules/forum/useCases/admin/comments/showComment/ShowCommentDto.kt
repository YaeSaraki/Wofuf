package dev.saraki.wofuf.modules.forum.useCases.admin.comments.showComment

class ShowCommentDto {
    data class Request(val commentId: String)
    data class Response(
        val commentId: String,
        val isHidden: Boolean,
        val message: String = "Comment shown successfully"
    )
}
