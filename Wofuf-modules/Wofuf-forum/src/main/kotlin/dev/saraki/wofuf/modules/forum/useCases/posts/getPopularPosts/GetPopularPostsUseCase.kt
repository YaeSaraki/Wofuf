package dev.saraki.wofuf.modules.forum.useCases.posts.getPopularPosts

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberDetails
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberDetailsProps
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.NickName
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteType
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostVotesRepo
import dev.saraki.wofuf.modules.forum.mappers.PostDtoMapper
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Use case for getting popular posts
 */
@Service
class GetPopularPostsUseCase(
    private val postRepo: PostRepo,
    private val memberRepo: MemberRepo,
    private val postVotesRepo: PostVotesRepo,
) : UseCase<GetPopularPostsDto.Request, GetPopularPostsDto.Response> {

    override fun execute(request: GetPopularPostsDto.Request): Result<GetPopularPostsDto.Response> {
        // 1. Validate offset
        if (request.offset != null && request.offset <= 0) {
            return GetPopularPostsErrors.InvalidOffsetError(request.offset)
        }

        // 2. Get current member if userId is provided
        var currentMemberId: MemberId? = null
        if (!request.userId.isNullOrBlank()) {
            val member = memberRepo.findMemberByUserId(
                dev.saraki.wofuf.modules.users.domain.valueObjects.UserId.create(
                    UniqueEntityId(request.userId)
                ).getOrNull() ?: return Result.success(GetPopularPostsDto.Response(emptyList()))
            )
            currentMemberId = member?.memberId
        }

        // 3. Find popular posts
        val posts = postRepo.findPopularPosts(request.offset)

        // 4. Map posts to DTOs with member details and comment counts
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
                // Fallback to unknown member
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

            // Get vote status if user is logged in
            val wasUpvotedByMe = currentMemberId?.let { memberId ->
                postVotesRepo.exists(post.postId, memberId, VoteType.UPVOTE)
            } ?: false

            val wasDownvotedByMe = currentMemberId?.let { memberId ->
                postVotesRepo.exists(post.postId, memberId, VoteType.DOWNVOTE)
            } ?: false

            // Map to DTO with vote status
            if (currentMemberId != null) {
                PostDtoMapper.toDtoWithVoteStatus(post, memberDetails, numComments, actualPoints, wasUpvotedByMe, wasDownvotedByMe)
            } else {
                PostDtoMapper.toDto(post, memberDetails, numComments, actualPoints)
            }
        }

        // 5. Return success response
        return Result.success(GetPopularPostsDto.Response(postDtos))
    }
}
