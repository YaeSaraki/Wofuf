package dev.saraki.wofuf.modules.forum.useCases.comments.getCommentByCommentId

import dev.saraki.wofuf.modules.forum.domain.valueObjects.CommentId
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.modules.forum.mappers.CommentDtoMapper
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/14 21:00
 *   @description:
 */
@Service
class GetCommentByCommentIdUseCase(
    private val commentRepo: CommentRepo,
    private val postRepo: PostRepo,
) : UseCase<GetCommentByCommentIdDto.Request, GetCommentByCommentIdDto.Response> {
    override fun execute(request: GetCommentByCommentIdDto.Request): Result<GetCommentByCommentIdDto.Response> {
        if (request.commentId.isBlank()) {
            return GetCommentByCommentIdErrors.CommentNotFoundError()
        }
        val commentIdOrError = CommentId.create(UniqueEntityId(request.commentId))

        if (commentIdOrError.isFailure) {
            return GetCommentByCommentIdErrors.CommentNotFoundError()
        }
        val commentId = commentIdOrError.getOrThrow()

        val comment = commentRepo.findCommentByCommentId(commentId) ?: return GetCommentByCommentIdErrors.CommentNotFoundError()
        val commentDetails =
            commentRepo.findCommentDetailsByCommentId(commentId) ?: return GetCommentByCommentIdErrors.CommentNotFoundError()

        val commentDto = CommentDtoMapper.toDto(comment, commentDetails)

        return Result.success(GetCommentByCommentIdDto.Response(commentDto))
    }
}
