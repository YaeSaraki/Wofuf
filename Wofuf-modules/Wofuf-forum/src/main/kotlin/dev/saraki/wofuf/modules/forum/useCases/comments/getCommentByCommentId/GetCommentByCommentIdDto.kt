package dev.saraki.wofuf.modules.forum.useCases.comments.getCommentByCommentId

import dev.saraki.wofuf.modules.forum.dtos.CommentDto
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/14 21:00
 *   @description: GetCommentByCommentId use case的DTO类，封装Request和Response
 */
class GetCommentByCommentIdDto {
    data class Request(
        val commentId: String,
        val userId: String? = null,
    )

    data class Response(
        val comment: CommentDto
    )
}
