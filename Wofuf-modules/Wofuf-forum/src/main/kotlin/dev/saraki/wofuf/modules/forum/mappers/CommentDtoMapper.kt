package dev.saraki.wofuf.modules.forum.mappers

import dev.saraki.wofuf.modules.forum.domain.Comment
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentDetails
import dev.saraki.wofuf.modules.forum.dtos.CommentDto

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/15 13:27
 *   @description:
 */
object CommentDtoMapper {
    fun toDto(
        comment: Comment,
        commentDetials: CommentDetails,
        wasUpvotedByMe: Boolean = false,
        wasDownvotedByMe: Boolean = false,
        replyToMemberNickname: String? = null,
        replyToShortId: String? = null,
        replyToParentCommentId: String? = null
    ): CommentDto =
        CommentDto(
            postSlug = commentDetials.postSlug.value,
            postTitle = commentDetials.postTitle.value,
            commentId = commentDetials.commentId.stringValue,
            shortId = commentDetials.shortId,
            parentCommentId = commentDetials.parentCommentId?.stringValue,
            rootCommentId = commentDetials.rootCommentId?.stringValue,
            text = commentDetials.text,
            memberId = comment.memberId.stringValue,
            memberNickname = commentDetials.memberDetails.nickName.value,
            playerId = commentDetials.memberDetails.playerId?.stringValue,
            createdAt = commentDetials.createdAt,
            childComments = emptyList(),
            points = commentDetials.points,
            wasUpvotedByMe = wasUpvotedByMe,
            wasDownvotedByMe = wasDownvotedByMe,
            isHidden = comment.isHidden,
            replyToMemberNickname = replyToMemberNickname,
            replyToShortId = replyToShortId,
            replyToParentCommentId = replyToParentCommentId
        )
}