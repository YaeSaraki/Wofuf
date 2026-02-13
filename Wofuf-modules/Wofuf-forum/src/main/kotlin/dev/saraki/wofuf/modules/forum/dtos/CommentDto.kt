package dev.saraki.wofuf.modules.forum.dtos

import dev.saraki.wofuf.modules.forum.domain.PostSlug
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/7 15:55
 *   @description:
 */
data class CommentDto(
    val postSlug: PostSlug,
    val postTitle: String,
    val commentId: String,
    val parentCommentId: String?,
    val text: String,
    val member: MemberDto,
    val createdAt: LocalDateTime,
    val childComments: List<CommentDto>,
    val points: Int,
    val wasUpvotedByMe: Boolean?,
    val wasDownvotedByMe: Boolean?
)
