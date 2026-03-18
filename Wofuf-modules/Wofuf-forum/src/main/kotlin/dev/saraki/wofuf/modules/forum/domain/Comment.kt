package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentText
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.AggregateRoot
import dev.saraki.wofuf.shared.domain.UniqueEntityId

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/6 13:30
 *   @description: Comment aggregate root entity
 */

data class CommentProps(
    val memberId: MemberId,
    val text: CommentText,
    val postId: PostId,
    val parentCommentId: CommentId?,
    var points: Int?
)

/**
 * Comment 实体 - 投票操作通过 CommentVoteDomainService 处理
 */
class Comment private constructor(
    props: CommentProps,
    id: UniqueEntityId?
) : AggregateRoot<CommentProps>(props, id) {

    val commentId: CommentId
        get() = CommentId.create(_id).getOrThrow()

    val memberId: MemberId
        get() = props.memberId

    val text: CommentText
        get() = props.text

    val postId: PostId
        get() = props.postId

    val parentCommentId: CommentId?
        get() = props.parentCommentId

    val points: Int
        get() = props.points ?: 0

    /**
     * 更新基础积分（总点赞-总点踩）
     * 由 CommentVoteDomainService 调用
     */
    fun updateScore(totalNumUpvotes: Int, totalNumDownvotes: Int) {
        props.points = totalNumUpvotes - totalNumDownvotes
    }

    fun editText(newText: CommentText): Result<Comment> {
        val newProps = props.copy(text = newText)
        return create(newProps, id)
    }

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

            val comment = Comment(props, id)

            return Result.success(comment)
        }
    }
}
