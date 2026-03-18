package dev.saraki.wofuf.modules.forum.useCases.posts.upvotePost

import dev.saraki.wofuf.modules.forum.domain.services.PostVoteDomainService
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Use case for upvoting a post (uses domain service)
 */
@Service
class UpvotePostUseCase(
    private val memberRepo: MemberRepo,
    private val postVoteDomainService: PostVoteDomainService,
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

        // 3. Get member by userId
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return UpvotePostErrors.MemberNotFoundError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()
        val member = memberRepo.findMemberByUserId(userId)
            ?: return UpvotePostErrors.MemberNotFoundError(request.userId)

        // 4. Use domain service to handle upvote
        val result = postVoteDomainService.upvote(postId, member.memberId)

        if (result.isFailure) {
            return UpvotePostErrors.UpvoteFailedError(request.postId)
        }

        val voteResult = result.getOrThrow()

        // 5. Return success response
        return Result.success(UpvotePostDto.Response(newPoints = voteResult.newPoints))
    }
}
