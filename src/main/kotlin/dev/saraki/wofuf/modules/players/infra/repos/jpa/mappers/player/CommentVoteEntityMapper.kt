package dev.saraki.wofuf.modules.players.infra.repos.jpa.mappers.player

import dev.saraki.wofuf.modules.forum.domain.CommentId
import dev.saraki.wofuf.modules.forum.domain.CommentVote
import dev.saraki.wofuf.modules.forum.domain.MemberId
import dev.saraki.wofuf.modules.forum.domain.VoteType
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.entities.CommentVoteEntity
import dev.saraki.wofuf.shared.domain.UniqueEntityId

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 17:55
 *   @description:
 */
object CommentVoteEntityMapper {

    fun toDomain(entity: CommentVoteEntity): CommentVote {
        val commentVote = CommentVote.create(
            commentId = CommentId.create(UniqueEntityId(entity.commentId)).getOrThrow(),
            memberId = MemberId.create(UniqueEntityId(entity.memberId)).getOrThrow(),
            voteType = when (entity.voteType) {
                "UPVOTE" -> VoteType.UPVOTE
                "DOWNVOTE" -> VoteType.DOWNVOTE
                else -> throw IllegalArgumentException("Invalid vote type: ${entity.voteType}")
            },
            id = UniqueEntityId(entity.voteId)
        ).getOrThrow()

        commentVote._createdAt = entity.createdAt
        commentVote._updatedAt = entity.updatedAt

        return commentVote
    }

    fun toEntity(domain: CommentVote): CommentVoteEntity {
        return CommentVoteEntity(
            voteId = domain._id.toString(),
            commentId = domain.commentId.stringValue,
            memberId = domain.memberId.stringValue,
            voteType = domain.voteType.toString()
        )
    }
}