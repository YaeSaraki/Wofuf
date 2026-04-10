package dev.saraki.wofuf.modules.forum.useCases.posts.getPostBySlug

import dev.saraki.wofuf.auth.infra.JwtAuthFilter
import dev.saraki.wofuf.modules.forum.domain.services.PostVoteDomainService
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberDetails
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberDetailsProps
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostStatus
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostVotesRepo
import dev.saraki.wofuf.modules.forum.mappers.PostDtoMapper
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Use case for getting a post by slug (uses domain service for vote status)
 */
@Service
class GetPostBySlugUseCase(
    private val postRepo: PostRepo,
    private val memberRepo: MemberRepo,
    private val postVotesRepo: PostVotesRepo,
    private val postVoteDomainService: PostVoteDomainService,
) : UseCase<GetPostBySlugDto.Request, GetPostBySlugDto.Response> {

    override fun execute(request: GetPostBySlugDto.Request): Result<GetPostBySlugDto.Response> {
        // 1. Validate post slug
        if (request.postSlug.isBlank()) {
            return GetPostBySlugErrors.PostSlugEmptyError()
        }

        // 2. Create PostSlug from existing slug
        val postSlugOrError = PostSlug.createFromExisting(request.postSlug)
        if (postSlugOrError.isFailure) {
            return GetPostBySlugErrors.PostNotFoundError(request.postSlug)
        }
        val postSlug = postSlugOrError.getOrThrow()

        // 3. Find the post by slug
        val post = postRepo.findPostBySlug(postSlug)
            ?: return GetPostBySlugErrors.PostNotFoundError(request.postSlug)

        // 4. Check if user can see hidden posts - if post is hidden and user can't see it, return 404
        val canSeeHidden = canSeeHiddenPosts()
        if (post.status == PostStatus.HIDDEN && !canSeeHidden) {
            // 隐藏的帖子对普通用户不可见（返回 404 以不暴露其存在）
            return GetPostBySlugErrors.PostNotFoundError(request.postSlug)
        }

        // 5. Get member details
        val member = memberRepo.findMemberById(post.memberId)
            ?: return GetPostBySlugErrors.MemberNotFoundError(request.postSlug)

        // 6. Create MemberDetails
        val memberDetailsOrError = MemberDetails.create(
            MemberDetailsProps(
                nickName = member.nickname,
                reputation = member.reputation,
                playerId = member.playerId
            )
        )
        if (memberDetailsOrError.isFailure) {
            return GetPostBySlugErrors.MemberNotFoundError(request.postSlug)
        }
        val memberDetails = memberDetailsOrError.getOrThrow()

        // 7. Get number of comments
        val numComments = postRepo.findNumberOfCommentsByPostId(post.postId) ?: 0

        // 8. Get current member if userId is provided
        var currentMemberId: MemberId? = null
        if (!request.userId.isNullOrBlank()) {
            val userIdOrError = UserId.create(
                UniqueEntityId(request.userId)
            )
            if (userIdOrError.isSuccess) {
                val currentUser = memberRepo.findMemberByUserId(userIdOrError.getOrThrow())
                currentMemberId = currentUser?.memberId
            }
        }

        // 9. Get vote status using domain service
        val voteStatus = currentMemberId?.let { memberId ->
            postVoteDomainService.getVoteStatus(post.postId, memberId)
        }

        // 10. Get actual points from database
        val totalUpvotes = postVotesRepo.countPostUpvotesByPostId(post.postId)
        val totalDownvotes = postVotesRepo.countPostDownvotesByPostId(post.postId)
        val actualPoints = totalUpvotes - totalDownvotes

        // 11. Map to DTO with vote status
        val postDto = if (voteStatus != null) {
            PostDtoMapper.toDtoWithVoteStatus(
                post, memberDetails, numComments, actualPoints,
                voteStatus.wasUpvotedByMe, voteStatus.wasDownvotedByMe
            )
        } else {
            PostDtoMapper.toDto(post, memberDetails, numComments, actualPoints)
        }

        // 12. Return success response
        return Result.success(GetPostBySlugDto.Response(postDto))
    }

    /**
     * 检查当前用户是否有权限查看隐藏帖子
     * - 未登录用户：不能查看隐藏帖子
     * - 普通用户：不能查看隐藏帖子
     * - 管理员：可以查看所有帖子
     * - 拥有 POST_HIDE 权限的用户：可以查看所有帖子
     */
    private fun canSeeHiddenPosts(): Boolean {
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

        // 检查是否有 POST_HIDE 权限
        return member.hasPermission(PermissionPoint.POST_HIDE)
    }
}
