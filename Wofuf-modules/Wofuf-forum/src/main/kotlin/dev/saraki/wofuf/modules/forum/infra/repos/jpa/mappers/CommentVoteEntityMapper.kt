package dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers

import dev.saraki.wofuf.modules.forum.domain.CommentId
import dev.saraki.wofuf.modules.forum.domain.CommentVote
import dev.saraki.wofuf.modules.forum.domain.MemberId
import dev.saraki.wofuf.modules.forum.domain.VoteType
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.CommentVoteEntity
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.domain.UniqueEntityId

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 18:06
 *   @description:
 */
object CommentVoteEntityMapper {

    fun toDomain(entity: CommentVoteEntity): CommentVote {
        val guardResult = Guard.againstNullOrUndefinedBulk(
            listOf(
                Guard.GuardArgument(entity.voteId, "voteId"),
                Guard.GuardArgument(entity.commentId, "commentId"),
                Guard.GuardArgument(entity.memberId, "memberId"),
                Guard.GuardArgument(entity.voteType, "voteType")
            )
        )

        if (guardResult.isFailure) {
            throw guardResult.exceptionOrThrow()
        }

        val commentVoteOrError = CommentVote.create(
            commentId = CommentId.create(UniqueEntityId(entity.commentId)).getOrThrow(),
            memberId = MemberId.create(UniqueEntityId(entity.memberId)).getOrThrow(),
            voteType = when (entity.voteType) {
                "UPVOTE" -> VoteType.UPVOTE
                "DOWNVOTE" -> VoteType.DOWNVOTE
                else -> throw IllegalArgumentException("Invalid vote type: ${entity.voteType}")
            },
            id = UniqueEntityId(entity.voteId)
        )

        val commentVote = commentVoteOrError.getOrThrow()

        commentVote._createdAt = entity.createdAt
        commentVote._updatedAt = entity.updatedAt

        return commentVote
    }

    fun toEntity(domain: CommentVote): CommentVoteEntity {
        return CommentVoteEntity(
            voteId = domain._id.uuid.toString(),
            commentId = domain.commentId.stringValue,
            memberId = domain.memberId.stringValue,
            voteType = domain.voteType.toString()
        )
    }
}