package dev.saraki.wofuf.modules.forum.infra.repos.impl

import dev.saraki.wofuf.modules.forum.domain.PostVote
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteType
import dev.saraki.wofuf.modules.forum.infra.repos.PostVotesRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.PostVotesJpaRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers.PostVoteEntityMapper
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/9 15:30
 *   @description: PostVotesRepo接口的实现类
 */
@Repository
class PostVotesRepoImpl(
    private val postVotesJpaRepo: PostVotesJpaRepo
) : PostVotesRepo {

    override fun exists(postId: PostId, memberId: MemberId, voteType: VoteType): Boolean =
        postVotesJpaRepo.existsByPostIdAndMemberIdAndVoteType(
            postId.stringValue,
            memberId.stringValue,
            voteType.toString()
        )

    override fun findByPostIdAndMemberId(postId: PostId, memberId: MemberId): List<PostVote> =
        postVotesJpaRepo.findByPostIdAndMemberId(
            postId.stringValue,
            memberId.stringValue
        ).map(PostVoteEntityMapper::toDomain)

    override fun countPostUpvotesByPostId(postId: PostId): Int =
        postVotesJpaRepo.countByPostIdAndVoteType(
            postId.stringValue,
            VoteType.UPVOTE.toString()
        )

    override fun countPostDownvotesByPostId(postId: PostId): Int =
        postVotesJpaRepo.countByPostIdAndVoteType(
            postId.stringValue,
            VoteType.DOWNVOTE.toString()
        )

    @Transactional
    override fun save(postVote: PostVote): PostVote {
        val entity = PostVoteEntityMapper.toEntity(postVote)
        return PostVoteEntityMapper.toDomain(postVotesJpaRepo.save(entity))
    }

    @Transactional
    override fun delete(postVote: PostVote) {
        val entity = PostVoteEntityMapper.toEntity(postVote)
        postVotesJpaRepo.deleteById(entity.voteId)
    }

    @Transactional
    override fun deleteByPostIdAndMemberId(postId: PostId, memberId: MemberId) {
        postVotesJpaRepo.deleteByPostIdAndMemberId(postId.stringValue, memberId.stringValue)
    }

    override fun findByPostIdsAndMemberId(postIds: List<String>, memberId: String): List<PostVote> =
        postVotesJpaRepo.findByPostIdInAndMemberId(postIds, memberId).map(PostVoteEntityMapper::toDomain)

    override fun flush() {
        postVotesJpaRepo.flush()
    }
}
