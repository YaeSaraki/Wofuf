package dev.saraki.wofuf.modules.forum.useCases.posts.upvotePost

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
 * @description Use case for upvoting a post
 */
@Service
class UpvotePostUseCase(
    private val postRepo: PostRepo,
    private val memberRepo: MemberRepo,
) : UseCase<UpvotePostDto.Request, UpvotePostDto.Response> {

    override fun execute(request: UpvotePostDto.Request): Result<UpvotePostDto.Response> {
        // 1. Validate inputs
        if (request.postId.isBlank()) {
            return UpvotePostErrors.PostIdEmptyError()
        }
        if (request.userId.isBlank()) {
            return UpvotePostErrors.UserIdEmptyError()
        }

        // 2. Validate and create PostId
        val postIdOrError = PostId.create(UniqueEntityId(request.postId))
        if (postIdOrError.isFailure) {
            return UpvotePostErrors.PostNotFoundError(request.postId)
        }
        val postId = postIdOrError.getOrThrow()

        // 3. Get the post
        val post = postRepo.findPostByPostId(postId)
            ?: return UpvotePostErrors.PostNotFoundError(request.postId)

        // 4. Get member by userId
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return UpvotePostErrors.MemberNotFoundError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()
        val member = memberRepo.findMemberByUserId(userId)
            ?: return UpvotePostErrors.MemberNotFoundError(request.userId)

        // 5. Check if already upvoted
        if (post.hasUpvotedBy(member.memberId)) {
            return UpvotePostErrors.AlreadyUpvotedError(request.postId, request.userId)
        }

        // 6. Check if downvoted - remove downvote first if exists
        val existingVote = post.getVoteByMember(member.memberId)
        if (existingVote != null && existingVote.isDownVote()) {
            post.removeVote(existingVote)
        }

        // 7. Create upvote
        val voteOrError = PostVote.createUpvote(postId, member.memberId)
        if (voteOrError.isFailure) {
            return UpvotePostErrors.UpvoteFailedError(request.postId)
        }
        val vote = voteOrError.getOrThrow()

        // 8. Add vote to post
        val addResult = post.addVote(vote)
        if (addResult.isFailure) {
            return UpvotePostErrors.UpvoteFailedError(request.postId)
        }

        // 9. Save the updated post
        val updatedPost = postRepo.save(post)

        // 10. Return success response
        return Result.success(UpvotePostDto.Response(newPoints = updatedPost.points))
    }
}
