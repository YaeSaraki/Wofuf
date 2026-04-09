package dev.saraki.wofuf.modules.forum.useCases.admin.posts.unpinPost

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class UnpinPostUseCase(
    private val postRepo: PostRepo,
) : UseCase<UnpinPostDto.Request, UnpinPostDto.Response> {

    @RequirePermission(PermissionPoint.POST_PIN, "Only users with POST_PIN permission can unpin posts")
    override fun execute(request: UnpinPostDto.Request): Result<UnpinPostDto.Response> {
        if (request.postId.isBlank()) {
            return UnpinPostErrors.PostIdEmptyError()
        }

        val postIdOrError = PostId.create(UniqueEntityId(request.postId))
        if (postIdOrError.isFailure) {
            return UnpinPostErrors.InvalidPostIdError(request.postId)
        }
        val postId = postIdOrError.getOrThrow()

        val post = postRepo.findPostByPostId(postId)
            ?: return UnpinPostErrors.PostNotFoundError(request.postId)

        val unpinResult = post.unpin()
        if (unpinResult.isFailure) {
            return UnpinPostErrors.UnpinFailedError(request.postId, unpinResult.exceptionOrThrow().message ?: "Unknown error")
        }

        try {
            postRepo.save(unpinResult.getOrThrow())
        } catch (e: Exception) {
            return UnpinPostErrors.SaveFailedError(request.postId)
        }

        return Result.success(UnpinPostDto.Response(postId = request.postId, isPinned = false))
    }
}
