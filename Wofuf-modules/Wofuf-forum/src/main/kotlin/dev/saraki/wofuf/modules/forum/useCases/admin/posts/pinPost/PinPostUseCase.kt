package dev.saraki.wofuf.modules.forum.useCases.admin.posts.pinPost

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/4/8
 * @description Use case for pinning a post
 */
@Service
class PinPostUseCase(
    private val postRepo: PostRepo,
) : UseCase<PinPostDto.Request, PinPostDto.Response> {

    @RequirePermission(PermissionPoint.POST_PIN, "Only users with POST_PIN permission can pin posts")
    override fun execute(request: PinPostDto.Request): Result<PinPostDto.Response> {
        println("[PinPostUseCase] Starting pin for post: ${request.postId}")
        
        // 1. Validate post ID
        if (request.postId.isBlank()) {
            return PinPostErrors.PostIdEmptyError()
        }

        // 2. Validate and create PostId
        val postIdOrError = PostId.create(UniqueEntityId(request.postId))
        if (postIdOrError.isFailure) {
            return PinPostErrors.InvalidPostIdError(request.postId)
        }
        val postId = postIdOrError.getOrThrow()

        // 3. Find the post
        val post = postRepo.findPostByPostId(postId)
            ?: return PinPostErrors.PostNotFoundError(request.postId)
        
        println("[PinPostUseCase] Found post ${request.postId}: isPinned=${post.isPinned}")

        // 4. Pin the post
        val pinResult = post.pin()
        if (pinResult.isFailure) {
            return PinPostErrors.PinFailedError(request.postId, pinResult.exceptionOrThrow().message ?: "Unknown error")
        }

        // 5. Save the post
        try {
            postRepo.save(pinResult.getOrThrow())
        } catch (e: Exception) {
            return PinPostErrors.SaveFailedError(request.postId)
        }

        // 6. Return success response
        return Result.success(PinPostDto.Response(
            postId = request.postId,
            isPinned = true
        ))
    }
}
