package dev.saraki.wofuf.modules.forum.infra.repos.impl

import dev.saraki.wofuf.modules.forum.domain.*
import dev.saraki.wofuf.modules.forum.infra.repos.PostVotesRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.PostVotesJpaRepo
import dev.saraki.wofuf.modules.forum.infra.repos.jpa.mappers.PostVoteEntityMapper
import org.springframework.stereotype.Repository

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

    override fun saveBulk(postVotes: PostVotes) {
        val newVoteEntities = postVotes.getNewItems().map(PostVoteEntityMapper::toEntity)
        val removedVoteEntities = postVotes.getRemovedItems().map(PostVoteEntityMapper::toEntity)
        postVotesJpaRepo.saveAll(newVoteEntities)
        postVotesJpaRepo.deleteAll(removedVoteEntities)
    }

    override fun save(postVote: PostVote): PostVote {
        val entity = PostVoteEntityMapper.toEntity(postVote)
        return PostVoteEntityMapper.toDomain(postVotesJpaRepo.save(entity))
    }

    override fun delete(postVote: PostVote) {
        val entity = PostVoteEntityMapper.toEntity(postVote)
        postVotesJpaRepo.deleteById(entity.voteId)
    }
}
