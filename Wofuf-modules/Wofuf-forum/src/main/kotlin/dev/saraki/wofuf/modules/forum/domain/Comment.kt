package dev.saraki.wofuf.modules.forum.domain

import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentText
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.shared.core.AppError
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.AggregateRoot
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import java.time.LocalDateTime

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
    val points: Int?,
    // 所属主评论ID（用于Bilibili风格评论）
    // 主评论的 rootCommentId = null
    // 子评论的 rootCommentId = 所属主评论的ID
    val rootCommentId: CommentId? = null,
    // 短 ID（用于显示和引用）
    val shortId: String? = null,
    // 管理功能相关字段
    val isHidden: Boolean = false,
    val hiddenAt: LocalDateTime? = null,
    val hiddenBy: MemberId? = null
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

    /**
     * 所属主评论ID（用于Bilibili风格评论）
     * 主评论返回 null
     * 子评论返回所属主评论的ID
     */
    val rootCommentId: CommentId?
        get() = props.rootCommentId

    /**
     * 短 ID（用于显示和引用）
     */
    val shortId: String?
        get() = props.shortId

    val points: Int
        get() = props.points ?: 0

    // 管理功能相关属性
    val isHidden: Boolean
        get() = props.isHidden

    val hiddenAt: LocalDateTime?
        get() = props.hiddenAt

    val hiddenBy: MemberId?
        get() = props.hiddenBy

    /**
     * 更新基础积分（总点赞-总点踩）
     * 由 CommentVoteDomainService 调用
     * 返回新的 Comment 实例（不可变模式）
     */
    fun updateScore(totalNumUpvotes: Int, totalNumDownvotes: Int): Comment {
        val newProps = props.copy(points = totalNumUpvotes - totalNumDownvotes)
        return create(newProps, _id).getOrThrow()
    }

    fun editText(newText: CommentText): Result<Comment> {
        val newProps = props.copy(text = newText)
        return create(newProps, id)
    }

    // ==================== 管理功能方法 ====================

    /**
     * 隐藏评论（幂等操作）
     * 如果已隐藏，直接返回当前评论（不报错）
     */
    fun hide(hiddenBy: MemberId): Result<Comment> {
        if (props.isHidden) {
            // 幂等：已隐藏则直接返回成功
            return Result.success(this)
        }
        val newProps = props.copy(
            isHidden = true,
            hiddenAt = LocalDateTime.now(),
            hiddenBy = hiddenBy
        )
        return create(newProps, id)
    }

    /**
     * 显示评论（取消隐藏，幂等操作）
     * 如果未隐藏，直接返回当前评论（不报错）
     */
    fun show(): Result<Comment> {
        if (!props.isHidden) {
            // 幂等：未隐藏则直接返回成功
            return Result.success(this)
        }
        val newProps = props.copy(
            isHidden = false,
            hiddenAt = null,
            hiddenBy = null
        )
        return create(newProps, id)
    }

    /**
     * 切换评论可见性（幂等操作）
     * 如果已隐藏，则显示；如果可见，则隐藏
     */
    fun toggleVisibility(toggledBy: MemberId): Result<Comment> {
        return if (props.isHidden) {
            show()
        } else {
            hide(toggledBy)
        }
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
