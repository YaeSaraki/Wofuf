package dev.saraki.wofuf.modules.forum.useCases.comments.getCommentByPostSlug

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.modules.forum.mappers.CommentDtoMapper
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/3/15 16:12
 *   @description:
 */
@Service
class GetCommentByPostSlugUseCase(
    private val commentRepo: CommentRepo,
    private val postRepo: PostRepo,
) : UseCase<GetCommentByPostSlugDto.Request, GetCommentByPostSlugDto.Response> {
    override fun execute(request: GetCommentByPostSlugDto.Request): Result<GetCommentByPostSlugDto.Response> {
        if (request.postSlug.isBlank()) {
            return GetCommentByPostSlugErrors.PostSlugEmptyError()
        }

        val postSlugOrError = PostSlug.createFromExisting(request.postSlug)
        if (postSlugOrError.isFailure) {
            return GetCommentByPostSlugErrors.PostNotFoundError()
        }
        val postSlug = postSlugOrError.getOrThrow()

        // Verify post exists
        val post = postRepo.findPostBySlug(postSlug) ?: return GetCommentByPostSlugErrors.PostNotFoundError()

        val comments = commentRepo.findCommentsByPostSlug(postSlug)
        if (comments.isEmpty()) {
            return GetCommentByPostSlugErrors.CommentsNotFoundError()
        }

        val commentDtos = comments.map { comment ->
            val commentDetails = commentRepo.findCommentDetailsByCommentId(comment.commentId)
            if (commentDetails != null) {
                CommentDtoMapper.toDto(comment, commentDetails)
            } else {
                null
            }
        }.filterNotNull()

        return Result.success(GetCommentByPostSlugDto.Response(commentDtos))
    }
}
