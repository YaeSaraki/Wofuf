package dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers

import dev.saraki.wofuf.modules.forum.domain.*
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.CommentEntity
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.PostEntity
import dev.saraki.wofuf.modules.forum.mappers.MemberDetailsMap
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 20:14
 *   @description:
 */
object CommentDetailsMapper {
    fun toDomain(commentEntity: CommentEntity): CommentDetails {
        val postEntity = commentEntity.postEntity!!
        val memberEntity = commentEntity.memberEntity!!
        val memberDetails = MemberDetailsMapper.toDomain(memberEntity)

        val parentCommentId = if (commentEntity.parentCommentId != null) {
            CommentId.create(UniqueEntityId(commentEntity.parentCommentId!!)).getOrNull()
        } else {
            null
        }

        val commentDetails = CommentDetails.create(
            CommentDetailsProps(
                commentId = CommentId.create(UniqueEntityId(commentEntity.commentId)).getOrThrow(),
                text = CommentText.create(commentEntity.text).getOrThrow(),
                member = memberDetails,
                createdAt = commentEntity.createdAt ?: LocalDateTime.now(),
                postSlug = PostSlug.createFromExisting(postEntity.slug).getOrThrow(),
                parentCommentId =  parentCommentId,
                points = commentEntity.points ?: -1,
                wasUpvotedByMe = null,
                wasDownvotedByMe = null
            )
        ).getOrThrow()
        return commentDetails
    }
}