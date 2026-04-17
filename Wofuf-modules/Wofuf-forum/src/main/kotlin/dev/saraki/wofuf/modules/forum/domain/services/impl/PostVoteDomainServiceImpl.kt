package dev.saraki.wofuf.modules.forum.domain.services.impl

import dev.saraki.wofuf.modules.forum.domain.Post
import dev.saraki.wofuf.modules.forum.domain.PostVote
import dev.saraki.wofuf.modules.forum.domain.services.PostVoteDomainService
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteResult
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteStatus
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteType
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostVotesRepo
import dev.saraki.wofuf.shared.core.AppError
import dev.saraki.wofuf.shared.core.Result
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PostVoteDomainServiceImpl(
    private val postVotesRepo: PostVotesRepo,
    private val postRepo: PostRepo
) : PostVoteDomainService {

    override fun getVoteStatus(postId: PostId, memberId: MemberId): VoteStatus {
        val wasUpvoted = postVotesRepo.exists(postId, memberId, VoteType.UPVOTE)
        val wasDownvoted = postVotesRepo.exists(postId, memberId, VoteType.DOWNVOTE)
        return VoteStatus(wasUpvoted, wasDownvoted)
    }

    override fun getVoteStatuses(
        postIds: List<PostId>,
        memberId: MemberId
    ): Map<PostId, VoteStatus> {
        if (postIds.isEmpty()) return emptyMap()

        val votes = postVotesRepo.findByPostIdsAndMemberId(
            postIds.map { it.stringValue },
            memberId.stringValue
        )

        return postIds.associateWith { postId ->
            val upvote = votes.any { vote ->
                vote.postId.stringValue == postId.stringValue && vote.type == VoteType.UPVOTE
            }
            val downvote = votes.any { vote ->
                vote.postId.stringValue == postId.stringValue && vote.type == VoteType.DOWNVOTE
            }
            VoteStatus(upvote, downvote)
        }
    }

    @Transactional
    override fun upvote(postId: PostId, memberId: MemberId): Result<VoteResult> {
        val post = postRepo.findPostByPostId(postId)
            ?: return Result.failure(AppError("Post not found", "POST_NOT_FOUND"))

        // 安全获取唯一投票记录
        val existing = postVotesRepo.findByPostIdAndMemberId(postId, memberId).firstOrNull()

        when {
            existing == null -> {
                // 无投票 → 新建
                val vote = PostVote.createUpvote(postId, memberId).getOrThrow()
                postVotesRepo.save(vote)
            }
            existing.isUpVote() -> {
                // 已点赞 → 取消
                postVotesRepo.delete(existing)
            }
            existing.isDownVote() -> {
                // 已点踩 → 直接切换为点赞（核心！无删无插，纯更新）
                existing.changeToUpVote()
                postVotesRepo.save(existing)
            }
        }

        return updatePostScore(post, postId)
    }

    @Transactional
    override fun downvote(postId: PostId, memberId: MemberId): Result<VoteResult> {
        val post = postRepo.findPostByPostId(postId)
            ?: return Result.failure(AppError("Post not found", "POST_NOT_FOUND"))

        val existing = postVotesRepo.findByPostIdAndMemberId(postId, memberId).firstOrNull()

        when {
            existing == null -> {
                val vote = PostVote.createDownvote(postId, memberId).getOrThrow()
                postVotesRepo.save(vote)
            }
            existing.isDownVote() -> {
                postVotesRepo.delete(existing)
            }
            existing.isUpVote() -> {
                // 已点赞 → 直接切换为点踩
                existing.changeToDownVote()
                postVotesRepo.save(existing)
            }
        }

        return updatePostScore(post, postId)
    }

    private fun updatePostScore(post: Post, postId: PostId): Result<VoteResult> {
        val up = postVotesRepo.countPostUpvotesByPostId(postId)
        val down = postVotesRepo.countPostDownvotesByPostId(postId)
        val updatedPost = post.updateScore(up, down)
        postRepo.save(updatedPost)
        return Result.success(VoteResult(up - down, postId.stringValue))
    }
}
