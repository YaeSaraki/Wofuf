package dev.saraki.wofuf.modules.forum.mappers

import dev.saraki.wofuf.modules.forum.domain.CommentDetails
import dev.saraki.wofuf.modules.forum.dtos.CommentDto

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 14:05
 *   @description:
 */
class CommentDetailsMap {
    fun from(commentDetails: CommentDetails): CommentDto {
        return CommentDto(
            postSlug = commentDetails.postSlug,
            postTitle = "", // 需要从Post中获取
            commentId = commentDetails.commentId.stringValue,
            parentCommentId = commentDetails.parentCommentId?.stringValue,
            text = commentDetails.text.value,
            member = MemberDetailsMap.from(commentDetails.member),
            createdAt = commentDetails.createdAt,
            childComments = emptyList(), // 需要单独获取
            points = commentDetails.points,
            wasUpvotedByMe = commentDetails.wasUpvotedByMe,
            wasDownvotedByMe = commentDetails.wasDownvotedByMe
        )
    }
}