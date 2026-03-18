package dev.saraki.wofuf.modules.forum.infra.repos.impl

import dev.saraki.wofuf.modules.forum.domain.CommentVote
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteType
import dev.saraki.wofuf.modules.forum.infra.repos.CommentVotesRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.CommentVotesJpaRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers.CommentVoteEntityMapper
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 15:30
 *   @description: CommentVotesRepo接口的实现类
 */
@Repository
class CommentVotesRepoImpl(
    private val commentVotesJpaRepo: CommentVotesJpaRepo
) : CommentVotesRepo {

    override fun exists(commentId: CommentId, memberId: MemberId, voteType: VoteType): Boolean {
        return commentVotesJpaRepo.existsByCommentIdAndMemberIdAndVoteType(
            commentId.stringValue,
            memberId.stringValue,
            voteType.name
        )
    }

    override fun findByCommentIdAndMemberId(commentId: CommentId, memberId: MemberId): CommentVote? {
        return commentVotesJpaRepo.findByCommentIdAndMemberId(
            commentId.stringValue,
            memberId.stringValue
        )?.let(CommentVoteEntityMapper::toDomain)
    }

    @Transactional
    override fun save(vote: CommentVote): CommentVote {
        val entity = CommentVoteEntityMapper.toEntity(vote)
        return CommentVoteEntityMapper.toDomain(commentVotesJpaRepo.save(entity))
    }

    @Transactional
    override fun delete(vote: CommentVote) {
        commentVotesJpaRepo.deleteById(vote.commentVoteId.stringValue)
    }

    override fun countCommentUpvotesByCommentId(commentId: CommentId): Int {
        return commentVotesJpaRepo.countUpvotesByCommentId(commentId.stringValue)
    }

    override fun countCommentDownvotesByCommentId(commentId: CommentId): Int {
        return commentVotesJpaRepo.countDownvotesByCommentId(commentId.stringValue)
    }

    override fun findByCommentIdsAndMemberId(commentIds: List<String>, memberId: String): List<CommentVote> {
        if (commentIds.isEmpty()) return emptyList()
        return commentVotesJpaRepo.findByCommentIdInAndMemberId(commentIds, memberId)
            .map(CommentVoteEntityMapper::toDomain)
    }

    override fun flush() {
        commentVotesJpaRepo.flush()
    }
}
