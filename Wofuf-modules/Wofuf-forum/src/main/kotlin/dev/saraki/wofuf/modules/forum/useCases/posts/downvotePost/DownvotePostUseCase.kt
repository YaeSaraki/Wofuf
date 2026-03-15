package dev.saraki.wofuf.modules.forum.useCases.posts.downvotePost

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostVote
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
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

        // 5. Check if already downvoted
        if (post.hasDownvotedBy(member.memberId)) {
            return DownvotePostErrors.AlreadyDownvotedError(request.postId, request.userId)
        }

        // 6. Check if upvoted - remove upvote first if exists
        val existingVote = post.getVoteByMember(member.memberId)
        if (existingVote != null && existingVote.isUpVote()) {
            post.removeVote(existingVote)
        }

        // 7. Create downvote
        val voteOrError = PostVote.createDownvote(postId, member.memberId)
        if (voteOrError.isFailure) {
            return DownvotePostErrors.DownvoteFailedError(request.postId)
        }
        val vote = voteOrError.getOrThrow()

        // 8. Add vote to post
        val addResult = post.addVote(vote)
        if (addResult.isFailure) {
            return DownvotePostErrors.DownvoteFailedError(request.postId)
        }

        // 9. Save the updated post
        val updatedPost = postRepo.save(post)

        // 10. Return success response
        return Result.success(DownvotePostDto.Response(newPoints = updatedPost.points))
    }
}
