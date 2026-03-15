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
    fun toDto(comment: Comment, commentDetials: CommentDetails): CommentDto =
        CommentDto(
            postSlug = commentDetials.postSlug.value,
            postTitle = commentDetials.postTitle.value,
            commentId = commentDetials.commentId.stringValue,
            parentCommentId = commentDetials.parentCommentId?.stringValue,
            text = commentDetials.text,
            memberId = comment.memberId.stringValue,
            createdAt = commentDetials.createdAt,
            childComments = emptyList(),
            points = commentDetials.points
        )
}