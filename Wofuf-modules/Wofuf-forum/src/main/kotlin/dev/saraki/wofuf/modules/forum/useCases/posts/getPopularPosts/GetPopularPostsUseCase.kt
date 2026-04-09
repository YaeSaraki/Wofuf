package dev.saraki.wofuf.modules.forum.useCases.posts.getPopularPosts

import dev.saraki.wofuf.auth.infra.JwtAuthFilter
import dev.saraki.wofuf.modules.forum.domain.services.PostVoteDomainService
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberDetails
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberDetailsProps
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.NickName
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostCategory
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
 * @description Use case for getting popular posts (uses domain service for vote status)
 */
@Service
class GetPopularPostsUseCase(
    private val postRepo: PostRepo,
    private val memberRepo: MemberRepo,
    private val postVotesRepo: PostVotesRepo,
    private val postVoteDomainService: PostVoteDomainService,
) : UseCase<GetPopularPostsDto.Request, GetPopularPostsDto.Response> {

    override fun execute(request: GetPopularPostsDto.Request): Result<GetPopularPostsDto.Response> {
        // 1. Validate page
        if (request.page < 0) {
            return GetPopularPostsErrors.InvalidPageError(request.page)
        }

        // 2. Parse category
        val category = request.category?.let { PostCategory.fromString(it) }

        // 3. Check if user can see hidden posts
        val includeHidden = canSeeHiddenPosts()

        // 4. Get current member if userId is provided
        var currentMemberId: MemberId? = null
        if (!request.userId.isNullOrBlank()) {

            val member = memberRepo.findMemberByUserId(
                UserId.create(
                    UniqueEntityId(request.userId)
                ).getOrNull() ?: return Result.success(GetPopularPostsDto.Response(emptyList()))
            )
            currentMemberId = member?.memberId
        }

        // 5. Find popular posts
        val posts = postRepo.findPopularPosts(request.page, request.size, category, includeHidden)


        // 6. Batch get vote statuses (避免 N+1 查询)
        val voteStatusMap = currentMemberId?.let { memberId ->
            postVoteDomainService.getVoteStatuses(posts.map { it.postId }, memberId)
        } ?: emptyMap()

        // 7. Map posts to DTOs with member details and comment counts
        val postDtos = posts.map { post ->
            // Get member details
            val member = memberRepo.findMemberById(post.memberId)
            val memberDetails = if (member != null) {
                MemberDetails.create(
                    MemberDetailsProps(
                        nickName = member.nickname,
                        reputation = member.reputation,
                        playerId = member.playerId
                    )
                ).getOrThrow()
            } else {
                MemberDetails.create(
                    MemberDetailsProps(
                        nickName = NickName.create("Unknown").getOrThrow(),
                        reputation = 0,
                        playerId = null
                    )
                ).getOrThrow()
            }

            // Get number of comments
            val numComments = postRepo.findNumberOfCommentsByPostId(post.postId) ?: 0

            // Get actual points from database
            val totalUpvotes = postVotesRepo.countPostUpvotesByPostId(post.postId)
            val totalDownvotes = postVotesRepo.countPostDownvotesByPostId(post.postId)
            val actualPoints = totalUpvotes - totalDownvotes

            // Get vote status from map
            val voteStatus = voteStatusMap[post.postId]
            val wasUpvotedByMe = voteStatus?.wasUpvotedByMe ?: false
            val wasDownvotedByMe = voteStatus?.wasDownvotedByMe ?: false

            // Map to DTO with vote status
            if (currentMemberId != null) {
                PostDtoMapper.toDtoWithVoteStatus(post, memberDetails, numComments, actualPoints, wasUpvotedByMe, wasDownvotedByMe)
            } else {
                PostDtoMapper.toDto(post, memberDetails, numComments, actualPoints)
            }
        }

        // 8. Return success response
        return Result.success(GetPopularPostsDto.Response(postDtos))
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
