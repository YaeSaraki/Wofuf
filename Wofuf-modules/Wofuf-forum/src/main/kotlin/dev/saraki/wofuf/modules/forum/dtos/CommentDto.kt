package dev.saraki.wofuf.modules.forum.dtos

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/7 15:55
 *   @description:
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
data class CommentDto(
    val postSlug: String,
    val postTitle: String,
    val commentId: String,
    val shortId: String?,            // 短 ID（用于显示和引用）
    val parentCommentId: String?,      // 直接回复的评论ID
    val rootCommentId: String?,       // 所属主评论ID（用于Bilibili风格）
    val text: String,
    val memberId: String,
    val memberNickname: String,        // 用户昵称
    val playerId: String?,             // 玩家UUID，用于获取皮肤
    val createdAt: LocalDateTime,
    val childComments: List<CommentDto>,
    val points: Int,
    val wasUpvotedByMe: Boolean = false,
    val wasDownvotedByMe: Boolean = false,
    @JsonProperty("isHidden")
    val isHidden: Boolean,             // 评论是否被隐藏（管理功能），由 mapper 根据 domain 设置
    val replyToMemberNickname: String? = null,  // 被回复者的昵称（用于显示"回复 @xxx"）
    val replyToShortId: String? = null,  // 被回复者的短 ID（用于点击跳转）
    val replyToParentCommentId: String? = null  // 被回复者的评论 ID（用于点击跳转定位）
)
