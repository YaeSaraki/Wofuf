package dev.saraki.wofuf.modules.forum.useCases.comments.getCommentByPostSlug

import dev.saraki.wofuf.auth.infra.JwtAuthFilter
import dev.saraki.wofuf.modules.forum.domain.services.CommentVoteDomainService
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteStatus
import dev.saraki.wofuf.modules.forum.infra.repos.CommentRepo
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.modules.forum.mappers.CommentDtoMapper
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
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
    private val memberRepo: MemberRepo,
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

        // Check if user can see hidden comments
        val includeHidden = canSeeHiddenComments()

        val comments = commentRepo.findCommentsByPostSlug(postSlug, includeHidden)

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

    /**
     * 检查当前用户是否有权限查看隐藏评论
     * - 未登录用户：不能查看隐藏评论
     * - 普通用户：不能查看隐藏评论
     * - 管理员：可以查看所有评论
     * - 拥有 COMMENT_VIEW_HIDDEN 权限的用户：可以查看所有评论
     */
    private fun canSeeHiddenComments(): Boolean {
        // 检查是否已登录（排除匿名用户）
        if (!JwtAuthFilter.isAuthenticated()) {
            return false
        }

        // 获取当前用户 ID
        val userId = JwtAuthFilter.getCurrentUserId()

        // 如果是匿名用户或 null，返回 false
        if (userId.isNullOrBlank() || userId == "anonymousUser") {
            return false
        }

        // 检查是否为系统管理员
        if (JwtAuthFilter.isAdmin()) {
            return true
        }

        // 查找论坛 Member 并检查权限
        val userIdObj = UserId.create(UniqueEntityId(userId)).getOrNull() ?: return false
        val member = memberRepo.findMemberByUserId(userIdObj) ?: return false

        // 检查是否有 COMMENT_VIEW_HIDDEN 权限
        return member.hasPermission(PermissionPoint.COMMENT_VIEW_HIDDEN)
    }
}
