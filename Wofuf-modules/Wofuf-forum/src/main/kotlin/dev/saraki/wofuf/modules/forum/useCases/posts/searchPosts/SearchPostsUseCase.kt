package dev.saraki.wofuf.modules.forum.useCases.posts.searchPosts

import dev.saraki.wofuf.auth.infra.JwtAuthFilter
import dev.saraki.wofuf.modules.forum.domain.services.PostVoteDomainService
import dev.saraki.wofuf.modules.forum.domain.valueObjects.*
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
 * Use case for searching posts by title or content
 */
@Service
class SearchPostsUseCase(
    private val postRepo: PostRepo,
    private val memberRepo: MemberRepo,
    private val postVotesRepo: PostVotesRepo,
    private val postVoteDomainService: PostVoteDomainService,
) : UseCase<SearchPostsDto.Request, SearchPostsDto.Response> {

    override fun execute(request: SearchPostsDto.Request): Result<SearchPostsDto.Response> {
        // 1. Validate query
        if (request.query.isBlank()) {
            return Result.success(SearchPostsDto.Response(emptyList()))
        }

        // 2. Validate page
        if (request.page < 0) {
            return Result.failure("Invalid page number")
        }

        // 3. Parse category
        val category = request.category?.let { PostCategory.fromString(it) }

        // 4. Search posts
        val posts = postRepo.searchPosts(request.query, request.page, request.size, category)

        // 5. Get current member
        val currentMemberId = getCurrentMemberId()

        // 6. Batch get vote statuses
        val voteStatusMap = currentMemberId?.let { memberId ->
            postVoteDomainService.getVoteStatuses(posts.map { it.postId }, memberId)
        } ?: emptyMap()

        // 7. Map posts to DTOs
        val postDtos = posts.map { post ->
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

            val numComments = postRepo.findNumberOfCommentsByPostId(post.postId) ?: 0
            val totalUpvotes = postVotesRepo.countPostUpvotesByPostId(post.postId)
            val totalDownvotes = postVotesRepo.countPostDownvotesByPostId(post.postId)
            val actualPoints = totalUpvotes - totalDownvotes

            val voteStatus = voteStatusMap[post.postId]
            val wasUpvotedByMe = voteStatus?.wasUpvotedByMe ?: false
            val wasDownvotedByMe = voteStatus?.wasDownvotedByMe ?: false

            if (currentMemberId != null) {
                PostDtoMapper.toDtoWithVoteStatus(post, memberDetails, numComments, actualPoints, wasUpvotedByMe, wasDownvotedByMe)
            } else {
                PostDtoMapper.toDto(post, memberDetails, numComments, actualPoints)
            }
        }

        return Result.success(SearchPostsDto.Response(postDtos))
    }

    /**
     * 获取当前登录用户的 MemberId
     */
    private fun getCurrentMemberId(): MemberId? {
        if (!JwtAuthFilter.isAuthenticated()) {
            return null
        }

        val userId = JwtAuthFilter.getCurrentUserId()
        if (userId.isNullOrBlank() || userId == "anonymousUser") {
            return null
        }

        val userIdObj = UserId.create(UniqueEntityId(userId)).getOrNull() ?: return null
        val member = memberRepo.findMemberByUserId(userIdObj) ?: return null
        return member.memberId
    }
}
