package dev.saraki.wofuf.modules.forum.useCases.comments.getCommentByPostSlug

import dev.saraki.wofuf.modules.forum.dtos.CommentDto

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/15 16:12
 *   @description: GetCommentByPostSlug use case的DTO类，封装Request和Response
 */
class GetCommentByPostSlugDto {
    data class Request(
        val postSlug: String,
        val userId: String? = null,
    )

    data class Response(
        val comments: List<CommentDto>
    )
}
