package dev.saraki.wofuf.modules.forum.useCases.admin.posts.approvePost

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class ApprovePostUseCase(
    private val postRepo: PostRepo,
) : UseCase<ApprovePostDto.Request, ApprovePostDto.Response> {

    @RequirePermission(PermissionPoint.POST_REVIEW, "Only users with POST_REVIEW permission can approve posts")
    override fun execute(request: ApprovePostDto.Request): Result<ApprovePostDto.Response> {
        if (request.postId.isBlank()) {
            return ApprovePostErrors.PostIdEmptyError()
        }

        val postIdOrError = PostId.create(UniqueEntityId(request.postId))
        if (postIdOrError.isFailure) {
            return ApprovePostErrors.InvalidPostIdError(request.postId)
        }
        val postId = postIdOrError.getOrThrow()

        val post = postRepo.findPostByPostId(postId)
            ?: return ApprovePostErrors.PostNotFoundError(request.postId)

        val approveResult = post.approve()
        if (approveResult.isFailure) {
            return ApprovePostErrors.ApproveFailedError(request.postId, approveResult.exceptionOrThrow().message ?: "Unknown error")
        }

        try {
            postRepo.save(approveResult.getOrThrow())
        } catch (e: Exception) {
            return ApprovePostErrors.SaveFailedError(request.postId)
        }

        return Result.success(ApprovePostDto.Response(postId = request.postId, status = "NORMAL"))
    }
}
