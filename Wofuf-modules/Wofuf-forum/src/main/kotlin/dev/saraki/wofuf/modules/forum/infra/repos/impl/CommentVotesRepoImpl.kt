package dev.saraki.wofuf.modules.forum.infra.repos.impl

import dev.saraki.wofuf.modules.forum.domain.*
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
        val voteType = voteType.name
        return commentVotesJpaRepo.existsByCommentIdAndMemberIdAndVoteType(
            commentId.stringValue,
            memberId.stringValue,
            voteType
        )
    }

    override fun findByCommentIdAndMemberId(commentId: CommentId, memberId: MemberId): CommentVote? {
        return commentVotesJpaRepo.findByCommentIdAndMemberId(
            commentId.stringValue,
            memberId.stringValue
        )?.let(CommentVoteEntityMapper::toDomain)
    }

    @Transactional
    override fun saveBulk(votes: CommentVotes) {
        commentVotesJpaRepo.saveAll(
            votes.getItems().map(CommentVoteEntityMapper::toEntity)
        )
    }

    @Transactional
    override fun save(vote: CommentVote): CommentVote {
        val entity = CommentVoteEntityMapper.toEntity(vote)
        return CommentVoteEntityMapper.toDomain(commentVotesJpaRepo.save(entity))
    }

    @Transactional
    override fun delete(vote: CommentVote) {
        commentVotesJpaRepo.deleteById(vote.commentId.stringValue)
    }
}