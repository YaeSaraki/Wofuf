package dev.saraki.wofuf.modules.forum.useCases.posts.downvotePost

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
 * @description Use case for downvoting a post (uses domain service)
 */
@Service
class DownvotePostUseCase(
    private val memberRepo: MemberRepo,
    private val postVoteDomainService: PostVoteDomainService,
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

        // 3. Get member by userId
        val userIdOrError = UserId.create(UniqueEntityId(request.userId))
        if (userIdOrError.isFailure) {
            return DownvotePostErrors.MemberNotFoundError(request.userId)
        }
        val userId = userIdOrError.getOrThrow()
        val member = memberRepo.findMemberByUserId(userId)
            ?: return DownvotePostErrors.MemberNotFoundError(request.userId)

        // 4. Use domain service to handle downvote
        val result = postVoteDomainService.downvote(postId, member.memberId)

        if (result.isFailure) {
            return DownvotePostErrors.DownvoteFailedError(request.postId)
        }

        val voteResult = result.getOrThrow()

        // 5. Return success response
        return Result.success(DownvotePostDto.Response(newPoints = voteResult.newPoints))
    }
}
