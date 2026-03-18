package dev.saraki.wofuf.modules.forum.useCases.comments.getCommentByPostSlug

import dev.saraki.wofuf.modules.forum.domain.services.CommentVoteDomainService
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteStatus
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
 *   @date 2026/3/15 16:12
 *   @description:
 */
@Service
class GetCommentByPostSlugUseCase(
    private val commentRepo: CommentRepo,
    private val postRepo: PostRepo,
    private val commentVoteDomainService: CommentVoteDomainService,
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

        // 解析用户ID用于查询投票状态
        val memberId = request.userId?.let { MemberId.create(UniqueEntityId(it)).getOrThrow() }

        // 批量获取投票状态（避免 N+1 查询）
        val voteStatusMap = memberId?.let {
            commentVoteDomainService.getVoteStatuses(comments.map { it.commentId }, it)
        } ?: emptyMap()

        val commentDtos = comments.map { comment ->
            val commentDetails = commentRepo.findCommentDetailsByCommentId(comment.commentId)
            if (commentDetails != null) {
                val voteStatus = voteStatusMap[comment.commentId] ?: VoteStatus.empty()
                CommentDtoMapper.toDto(comment, commentDetails, voteStatus.wasUpvotedByMe, voteStatus.wasDownvotedByMe)
            } else {
                null
            }
        }.filterNotNull()

        return Result.success(GetCommentByPostSlugDto.Response(commentDtos))
    }
}
