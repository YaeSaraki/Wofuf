package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.domain.Entity

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/6 13:30
 *   @description:
 */
import dev.saraki.wofuf.shared.domain.UniqueEntityId

data class CommentProps(
    val memberId: MemberId,
    val text: CommentText,
    val postId: PostId,
    val parentCommentId: CommentId? = null,
    var points: Int? = null, // 可变：需支持updateScore更新
    var votes: CommentVotes  // 可变：需支持add/removeVote，非空（create时填充默认值）
)

class Comment private constructor(
    props: CommentProps,
    id: UniqueEntityId? = null
) : Entity<CommentProps>(props, id) {

    val commentId: CommentId
        get() = CommentId.create(_id).getOrThrow()

    val postId: PostId
        get() = props.postId

    val parentCommentId: CommentId?
        get() = props.parentCommentId

    val memberId: MemberId
        get() = props.memberId

    val text: CommentText
        get() = props.text

    val points: Int
        get() {
            val initialValue = props.points ?: 0
            return initialValue + computeVotePoints()
        }

    /**
     * 新添加的投票：UPVOTE+1 / DOWNVOTE-1
     * 被移除的投票：UPVOTE-1 / DOWNVOTE+1
     */
    private fun computeVotePoints(): Int {
        var tally = 0
        // 遍历新添加的投票，计算积分
        props.votes.getNewItems().forEach { vote ->
            if (vote.isUpVote()) tally++
            if (vote.isDownVote()) tally--
        }
        // 遍历被移除的投票，回滚积分
        props.votes.getRemovedItems().forEach { vote ->
            if (vote.isUpVote()) tally--
            if (vote.isDownVote()) tally++
        }
        return tally
    }

    fun removeVote(vote: CommentVote): Result<Unit> {
        props.votes.remove(vote)
        return Result.success(Unit)
    }

    fun addVote(vote: CommentVote): Result<Unit> {
        props.votes.add(vote)
        return Result.success(Unit)
    }

    fun getVotes(): CommentVotes = props.votes

    /**
     * 更新基础积分（总点赞-总点踩）
     * 用于从持久化层加载最新基础积分，而非计算内存中的变更
     */
    fun updateScore(totalNumUpvotes: Int, totalNumDownvotes: Int) {
        props.points = totalNumUpvotes - totalNumDownvotes
    }

    /**
     * 私有构造+工厂方法，保证实体创建的合法性校验
     */
    companion object {
        fun create(props: CommentProps, id: UniqueEntityId? = null): Result<Comment> {
            val nullGuard = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(props.memberId, "memberId"),
                    Guard.GuardArgument(props.text, "text"),
                    Guard.GuardArgument(props.postId, "postId")
                )
            )
            if (nullGuard.isFailure) {
                return Result.failure(nullGuard.exceptionOrThrow())
            }

            val isNewComment = id == null

            val defaultProps = CommentProps(
                memberId = props.memberId,
                text = props.text,
                postId = props.postId,
                parentCommentId = props.parentCommentId,
                points = props.points ?: 0,
                votes = props.votes
            )

            val comment = Comment(defaultProps, id)

            if (isNewComment) {
                val upvote = CommentVote.createUpvote( comment.commentId, props.memberId).getOrThrow()
                comment.addVote(upvote)
            }

            // 校验成功，返回成功Result
            return Result.success(comment)
        }
    }
}