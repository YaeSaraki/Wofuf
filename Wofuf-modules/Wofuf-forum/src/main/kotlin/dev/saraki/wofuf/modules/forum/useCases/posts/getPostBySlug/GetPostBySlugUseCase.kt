package dev.saraki.wofuf.modules.forum.useCases.posts.getPostBySlug

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberDetails
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberDetailsProps
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostSlug
import dev.saraki.wofuf.modules.forum.domain.valueObjects.VoteType
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
 * @description Use case for getting a post by slug
 */
@Service
class GetPostBySlugUseCase(
    private val postRepo: PostRepo,
    private val memberRepo: MemberRepo,
    private val postVotesRepo: PostVotesRepo,
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

        // 4. Get member details
        val member = memberRepo.findMemberById(post.memberId)
            ?: return GetPostBySlugErrors.MemberNotFoundError(request.postSlug)

        // 5. Create MemberDetails
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

        // 6. Get number of comments
        val numComments = postRepo.findNumberOfCommentsByPostId(post.postId) ?: 0

        // 7. Get current member if userId is provided
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

        // 8. Get vote status if user is logged in
        val wasUpvotedByMe = currentMemberId?.let { memberId ->
            postVotesRepo.exists(post.postId, memberId, VoteType.UPVOTE)
        } ?: false

        val wasDownvotedByMe = currentMemberId?.let { memberId ->
            postVotesRepo.exists(post.postId, memberId, VoteType.DOWNVOTE)
        } ?: false

        // 9. Get actual points from database
        val totalUpvotes = postVotesRepo.countPostUpvotesByPostId(post.postId)
        val totalDownvotes = postVotesRepo.countPostDownvotesByPostId(post.postId)
        val actualPoints = totalUpvotes - totalDownvotes

        // 10. Map to DTO with vote status
        val postDto = if (currentMemberId != null) {
            PostDtoMapper.toDtoWithVoteStatus(post, memberDetails, numComments, actualPoints, wasUpvotedByMe, wasDownvotedByMe)
        } else {
            PostDtoMapper.toDto(post, memberDetails, numComments, actualPoints)
        }

        // 11. Return success response
        return Result.success(GetPostBySlugDto.Response(postDto))
    }
}
