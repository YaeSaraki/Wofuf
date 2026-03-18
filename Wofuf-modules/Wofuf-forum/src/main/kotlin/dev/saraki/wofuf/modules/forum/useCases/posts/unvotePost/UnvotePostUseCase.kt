package dev.saraki.wofuf.modules.forum.useCases.posts.unvotePost

import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostVotesRepo
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/17
 * @description Use case for removing a vote from a post
 */
@Service
class UnvotePostUseCase(
    private val postRepo: PostRepo,
    private val memberRepo: MemberRepo,
    private val postVotesRepo: PostVotesRepo,
) : UseCase<UnvotePostDto.Request, UnvotePostDto.Response> {

    override fun execute(request: UnvotePostDto.Request): Result<UnvotePostDto.Response> {
        // 1. Validate inputs
        if (request.postId.isBlank()) {
            return UnvotePostErrors.PostIdEmptyError()
        }
        if (request.userId.isBlank()) {
            return UnvotePostErrors.UserIdEmptyError()
        }

        // 2. Validate and create PostId
        val postIdOrError = PostId.create(UniqueEntityId(request.postId))
        if (postIdOrError.isFailure) {
            return UnvotePostErrors.PostNotFoundError(request.postId)
        }
        val postId = postIdOrError.getOrThrow()

        // 3. Get the post
        val post = postRepo.findPostByPostId(postId)
            ?: return UnvotePostErrors.PostNotFoundError(request.postId)

        // 4. Get member by userId
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return UnvotePostErrors.MemberNotFoundError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()
        val member = memberRepo.findMemberByUserId(userId)
            ?: return UnvotePostErrors.MemberNotFoundError(request.userId)

        // 5. Get existing vote by member
        val existingVotes = postVotesRepo.findByPostIdAndMemberId(postId, member.memberId)
        if (existingVotes.isEmpty()) {
            return UnvotePostErrors.NoVoteToRemoveError(request.postId, request.userId)
        }

        // 6. Remove the vote from repo and update post score
        val existingVote = existingVotes.first()
        postVotesRepo.delete(existingVote)

        // 7. Update post score
        post.updateScore(
            postVotesRepo.countPostUpvotesByPostId(postId),
            postVotesRepo.countPostDownvotesByPostId(postId)
        )

        // 8. Save the updated post
        val updatedPost = postRepo.save(post)

        // 9. Return success response
        return Result.success(UnvotePostDto.Response(newPoints = updatedPost.points))
    }
}
