package dev.saraki.wofuf.modules.forum.infra.commentVote

import dev.saraki.wofuf.modules.forum.domain.Comment
import dev.saraki.wofuf.modules.forum.domain.CommentVote
import dev.saraki.wofuf.modules.forum.domain.services.CommentVoteDomainService
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteResult
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteStatus
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteType
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.CommentVotesRepo
import dev.saraki.wofuf.shared.core.Result
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommentVoteDomainServiceImpl(
    private val commentVotesRepo: CommentVotesRepo,
    private val commentRepo: CommentRepo
) : CommentVoteDomainService {

    override fun getVoteStatus(commentId: CommentId, memberId: MemberId): VoteStatus {
        val wasUpvoted = commentVotesRepo.exists(commentId, memberId, VoteType.UPVOTE)
        val wasDownvoted = commentVotesRepo.exists(commentId, memberId, VoteType.DOWNVOTE)
        return VoteStatus(wasUpvoted, wasDownvoted)
    }

    override fun getVoteStatuses(
        commentIds: List<CommentId>,
        memberId: MemberId
    ): Map<CommentId, VoteStatus> {
        if (commentIds.isEmpty()) return emptyMap()

        val votes = commentVotesRepo.findByCommentIdsAndMemberId(
            commentIds.map { it.stringValue },
            memberId.stringValue
        )

        return commentIds.associateWith { commentId ->
            val upvote = votes.any { vote ->
                vote.commentId.stringValue == commentId.stringValue && vote.voteType == VoteType.UPVOTE
            }
            val downvote = votes.any { vote ->
                vote.commentId.stringValue == commentId.stringValue && vote.voteType == VoteType.DOWNVOTE
            }
            VoteStatus(upvote, downvote)
        }
    }

    @Transactional
    override fun upvote(commentId: CommentId, memberId: MemberId): Result<VoteResult> {
        val comment = commentRepo.findCommentByCommentId(commentId)
            ?: return Result.failure("Comment not found: ${commentId.stringValue}")

        val existing = commentVotesRepo.findByCommentIdAndMemberId(commentId, memberId)

        when {
            existing == null -> {
                // 无记录 → 新建点赞
                val vote = CommentVote.createUpvote(commentId, memberId).getOrThrow()
                commentVotesRepo.save(vote)
            }
            existing.isUpVote() -> {
                // 已点赞 → 取消
                commentVotesRepo.delete(existing)
            }
            existing.isDownVote() -> {
                // 已点踩 → 直接切换为点赞（无删无插，纯更新）
                existing.changeToUpVote()
                commentVotesRepo.save(existing)
            }
        }

        return updateCommentScore(comment, commentId)
    }

    @Transactional
    override fun downvote(commentId: CommentId, memberId: MemberId): Result<VoteResult> {
        val comment = commentRepo.findCommentByCommentId(commentId)
            ?: return Result.failure("Comment not found: ${commentId.stringValue}")

        val existing = commentVotesRepo.findByCommentIdAndMemberId(commentId, memberId)

        when {
            existing == null -> {
                // 无记录 → 新建点踩
                val vote = CommentVote.createDownvote(commentId, memberId).getOrThrow()
                commentVotesRepo.save(vote)
            }
            existing.isDownVote() -> {
                // 已点踩 → 取消
                commentVotesRepo.delete(existing)
            }
            existing.isUpVote() -> {
                // 已点赞 → 直接切换为点踩
                existing.changeToDownVote()
                commentVotesRepo.save(existing)
            }
        }

        return updateCommentScore(comment, commentId)
    }

    private fun updateCommentScore(comment: Comment, commentId: CommentId): Result<VoteResult> {
        val up = commentVotesRepo.countCommentUpvotesByCommentId(commentId)
        val down = commentVotesRepo.countCommentDownvotesByCommentId(commentId)
        comment.updateScore(up, down)
        commentRepo.save(comment)
        return Result.success(VoteResult(up - down, commentId.stringValue))
    }
}