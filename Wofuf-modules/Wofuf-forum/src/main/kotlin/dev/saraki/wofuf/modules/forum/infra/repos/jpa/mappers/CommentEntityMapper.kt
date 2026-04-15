package dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers

import dev.saraki.wofuf.modules.forum.domain.Comment
import dev.saraki.wofuf.modules.forum.domain.CommentProps
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentDetails
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentDetailsProps
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentText
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostTitle
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.CommentEntity
import dev.saraki.wofuf.shared.domain.UniqueEntityId

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 17:29
 *   @description:
 */
object CommentEntityMapper {

    fun toDomain(entity: CommentEntity): Comment {
        val commentOrError = Comment.create(
            props = CommentProps(
                memberId = MemberId.create(UniqueEntityId(entity.memberId)).getOrThrow(),
                text = CommentText.create(entity.text).getOrThrow(),
                postId = PostId.create(UniqueEntityId(entity.postId)).getOrThrow(),
                parentCommentId = entity.parentCommentId?.let { CommentId.create(UniqueEntityId(it)).getOrThrow() },
                rootCommentId = entity.rootCommentId?.let { CommentId.create(UniqueEntityId(it)).getOrThrow() },
                shortId = entity.shortId,
                points = entity.points,
                // 管理功能相关字段
                isHidden = entity.isHidden,
                hiddenAt = entity.hiddenAt,
                hiddenBy = entity.hiddenBy?.let { MemberId.create(UniqueEntityId(it)).getOrThrow() }
            ),
            id = UniqueEntityId(entity.commentId)
        )

        val comment = commentOrError.getOrThrow()

        comment._createdAt = entity.createdAt
        comment._updatedAt = entity.updatedAt

        return comment
    }

    fun toCommentDetails(commentEntity: CommentEntity): CommentDetails {
        return CommentDetails.create(
            CommentDetailsProps(
                commentId = CommentId.create(UniqueEntityId(commentEntity.commentId)).getOrThrow(),
                text = commentEntity.text,
                memberDetails = MemberEntityMapper.toMemberDetails(commentEntity.memberEntity!!),
                postSlug = PostSlug.createFromExisting(commentEntity.postEntity!!.slug).getOrThrow(),
                postTitle = PostTitle.create(commentEntity.postEntity!!.title).getOrThrow(),
                parentCommentId = commentEntity.parentCommentId?.let { CommentId.create(UniqueEntityId(it)).getOrThrow() },
                rootCommentId = commentEntity.rootCommentId?.let { CommentId.create(UniqueEntityId(it)).getOrThrow() },
                shortId = commentEntity.shortId,
                points = commentEntity.points,
                createdAt = commentEntity.createdAt,
            )
        ).getOrThrow()
    }

    fun toEntity(domain: Comment): CommentEntity {
        return CommentEntity(
            commentId = domain.commentId.stringValue,
            shortId = domain.shortId,
            memberId = domain.memberId.stringValue,
            text = domain.text.value,
            postId = domain.postId.stringValue,
            parentCommentId = domain.parentCommentId?.stringValue,
            rootCommentId = domain.rootCommentId?.stringValue,
            points = domain.points,
            // 管理功能相关字段
            isHidden = domain.isHidden,
            hiddenAt = domain.hiddenAt,
            hiddenBy = domain.hiddenBy?.stringValue
        )
    }
}