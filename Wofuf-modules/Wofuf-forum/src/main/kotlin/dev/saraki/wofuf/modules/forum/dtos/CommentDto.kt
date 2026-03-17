package dev.saraki.wofuf.modules.forum.dtos

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/7 15:55
 *   @description:
 */
data class CommentDto(
    val postSlug: String,
    val postTitle: String,
    val commentId: String,
    val parentCommentId: String?,
    val text: String,
    val memberId: String,
    val memberNickname: String,  // 用户昵称
    val playerId: String?,  // 玩家UUID，用于获取皮肤
    val createdAt: LocalDateTime,
    val childComments: List<CommentDto>,
    val points: Int,
)
