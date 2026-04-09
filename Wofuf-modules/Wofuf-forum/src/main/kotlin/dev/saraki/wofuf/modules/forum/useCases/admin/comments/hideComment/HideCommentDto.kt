package dev.saraki.wofuf.modules.forum.useCases.admin.comments.hideComment

class HideCommentDto {
    data class Request(
        val commentId: String,
        val hiddenByMemberId: String
    )
    data class Response(
        val commentId: String,
        val isHidden: Boolean,
        val message: String = "Comment hidden successfully"
    )
}
