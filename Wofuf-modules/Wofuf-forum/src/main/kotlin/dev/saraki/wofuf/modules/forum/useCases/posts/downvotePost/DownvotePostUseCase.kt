package dev.saraki.wofuf.modules.forum.useCases.posts.downvotePost

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.domain.PostVote
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
 * @date 2026/3/15
 * @description Use case for downvoting a post
 */
@Service
class DownvotePostUseCase(
    private val postRepo: PostRepo,
    private val memberRepo: MemberRepo,
    private val postVotesRepo: PostVotesRepo,
) : UseCase<DownvotePostDto.Request, DownvotePostDto.Response> {

    override fun execute(request: DownvotePostDto.Request): Result<DownvotePostDto.Response> {
        // 1. Validate inputs
        if (request.postId.isBlank()) {
            return DownvotePostErrors.PostIdEmptyError()
        }
        if (request.userId.isBlank()) {
            return DownvotePostErrors.UserIdEmptyError()
        }

        // 2. Validate and create PostId
        val postIdOrError = PostId.create(UniqueEntityId(request.postId))
        if (postIdOrError.isFailure) {
            return DownvotePostErrors.PostNotFoundError(request.postId)
        }
        val postId = postIdOrError.getOrThrow()

        // 3. Get the post
        val post = postRepo.findPostByPostId(postId)
            ?: return DownvotePostErrors.PostNotFoundError(request.postId)

        // 4. Get member by userId
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return DownvotePostErrors.MemberNotFoundError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()
        val member = memberRepo.findMemberByUserId(userId)
            ?: return DownvotePostErrors.MemberNotFoundError(request.userId)

        // 5. Query existing vote from database
        val existingVotes = postVotesRepo.findByPostIdAndMemberId(postId, member.memberId)
        
        // 6. Check if already downvoted in database
        val existingDownvote = existingVotes.find { it.isDownVote() }
        if (existingDownvote != null) {
            // User wants to remove their downvote (toggle off)
            postVotesRepo.delete(existingDownvote)
        } else {
            // 7. Delete any existing upvote (switching from upvote to downvote)
            existingVotes.filter { it.isUpVote() }.forEach { existingVote ->
                postVotesRepo.delete(existingVote)
            }

            // 8. Create downvote
            val voteOrError = PostVote.createDownvote(postId, member.memberId)
            if (voteOrError.isFailure) {
                return DownvotePostErrors.DownvoteFailedError(request.postId)
            }
            val vote = voteOrError.getOrThrow()

            // 9. Save the new vote
            postVotesRepo.save(vote)
        }

        // 10. Update post points and save
        val totalUpvotes = postVotesRepo.countPostUpvotesByPostId(postId)
        val totalDownvotes = postVotesRepo.countPostDownvotesByPostId(postId)
        post.updateScore(totalUpvotes, totalDownvotes)
        postRepo.save(post)

        // 11. Return success response
        return Result.success(DownvotePostDto.Response(newPoints = totalUpvotes - totalDownvotes))
    }
}
