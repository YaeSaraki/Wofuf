package dev.saraki.wofuf.modules.forum.useCases.comments.updateCommentStats

import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15 16:15
 * @description Update comment statistics (e.g., points based on votes)
 */
@Service
class UpdateCommentStatsUseCase(
    private val commentRepo: CommentRepo,
) : UseCase<UpdateCommentStatsDto.Request, UpdateCommentStatsDto.Response> {
    override fun execute(request: UpdateCommentStatsDto.Request): Result<UpdateCommentStatsDto.Response> {
        if (request.commentId.isBlank()) {
            return UpdateCommentStatsErrors.CommentIdEmptyError()
        }

        // Validate comment ID
        val commentIdOrError = CommentId.create(UniqueEntityId(request.commentId))
        if (commentIdOrError.isFailure) {
            return UpdateCommentStatsErrors.CommentNotFoundError(request.commentId)
        }
        val commentId = commentIdOrError.getOrThrow()

        // Get comment
        val comment = commentRepo.findCommentByCommentId(commentId) ?: return UpdateCommentStatsErrors.CommentNotFoundError(request.commentId)

        // Assume we have a way to get vote stats (this would need to be added to CommentRepo)
        // For now, we'll simulate by getting current points
        val currentPoints = comment.points

        // In a real implementation, you would query the database for total upvotes and downvotes
        // and call comment.updateScore(totalUpvotes, totalDownvotes)

        // For this example, we'll just save the comment (which might trigger recalculation)
        val updatedComment = commentRepo.save(comment)

        return Result.success(UpdateCommentStatsDto.Response(updatedPoints = updatedComment.points))
    }
}
