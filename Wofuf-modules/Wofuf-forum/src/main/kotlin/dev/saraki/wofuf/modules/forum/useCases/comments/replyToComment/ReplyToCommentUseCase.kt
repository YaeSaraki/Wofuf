package dev.saraki.wofuf.modules.forum.useCases.comments.replyToComment

import dev.saraki.wofuf.modules.forum.domain.Comment
import dev.saraki.wofuf.modules.forum.domain.CommentProps
import dev.saraki.wofuf.modules.forum.domain.utils.ShortIdGenerator
import dev.saraki.wofuf.modules.forum.domain.valueObjects.*
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import org.springframework.stereotype.Service

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/15 16:15
 *   @description:
 */
@Service
class ReplyToCommentUseCase(
    private val memberRepo: MemberRepo,
    private val postRepo: PostRepo,
    private val commentRepo: CommentRepo,
) : UseCase<ReplyToCommentDto.Request, ReplyToCommentDto.Response> {
    override fun execute(request: ReplyToCommentDto.Request): Result<ReplyToCommentDto.Response> {
        if (request.postSlug.isBlank()) {
            return ReplyToCommentErrors.PostSlugEmptyError()
        }

        if (request.comment.isBlank()) {
            return ReplyToCommentErrors.CommentTextEmptyError()
        }

        // Validate post slug
        val postSlugOrError = PostSlug.createFromExisting(request.postSlug)
        if (postSlugOrError.isFailure) {
            return ReplyToCommentErrors.PostNotFoundError(request.postSlug)
        }
        val postSlug = postSlugOrError.getOrThrow()

        // Get post
        val post = postRepo.findPostBySlug(postSlug) ?: return ReplyToCommentErrors.PostNotFoundError(request.postSlug)

        // Get member
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return ReplyToCommentErrors.MemberNotFoundError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()
        val member = memberRepo.findMemberByUserId(userId) ?: return ReplyToCommentErrors.MemberNotFoundError(request.userId)

        // Get parent comment
        val parentCommentIdOrError = CommentId.create(UniqueEntityId(request.parentCommentId))
        if (parentCommentIdOrError.isFailure) {
            return ReplyToCommentErrors.CommentNotFoundError(request.parentCommentId)
        }
        val parentCommentId = parentCommentIdOrError.getOrThrow()
        val parentComment = commentRepo.findCommentByCommentId(parentCommentId) ?: return ReplyToCommentErrors.CommentNotFoundError(request.parentCommentId)

        // Validate comment text
        val commentTextOrError = CommentText.create(request.comment)
        if (commentTextOrError.isFailure) {
            return ReplyToCommentErrors.CommentTextEmptyError()
        }
        val commentText = commentTextOrError.getOrThrow()

        // 计算 rootCommentId（用于 Bilibili 风格评论）
        // 如果父评论已经是子评论（rootCommentId != null），则沿用 rootCommentId
        // 如果父评论是主评论（rootCommentId == null），则 rootCommentId = 父评论ID
        val effectiveRootCommentId = parentComment.rootCommentId ?: parentComment.commentId

        // 生成短 ID（使用父评论 ID 作为盐值确保在同一帖子内唯一）
        val shortId = ShortIdGenerator.generateFromString("${post.postId.stringValue}_${parentCommentId.stringValue}_${System.nanoTime()}")

        // Create comment as a reply
        val commentProps = CommentProps(
            memberId = member.memberId,
            text = commentText,
            postId = post.postId,
            parentCommentId = parentCommentId,
            rootCommentId = effectiveRootCommentId,
            shortId = shortId,
            points = 0
        )
        val commentOrError = Comment.create(commentProps)
        if (commentOrError.isFailure) {
            return Result.failure(commentOrError.exceptionOrThrow())
        }

        // Save the comment
        commentRepo.save(commentOrError.getOrThrow())

        return Result.success(ReplyToCommentDto.Response())
    }
}
