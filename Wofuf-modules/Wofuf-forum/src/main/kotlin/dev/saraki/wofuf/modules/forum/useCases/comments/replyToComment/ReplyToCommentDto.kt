package dev.saraki.wofuf.modules.forum.useCases.comments.replyToComment

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/15 16:15
 *   @description: ReplyToComment use case的DTO类，封装Request和Response
 */
class ReplyToCommentDto {
    data class Request(
        val postSlug: String,
        val userId: String,
        val comment: String,
        val parentCommentId: String,
    )

    data class Response(
        val success: Boolean = true
    )
}
