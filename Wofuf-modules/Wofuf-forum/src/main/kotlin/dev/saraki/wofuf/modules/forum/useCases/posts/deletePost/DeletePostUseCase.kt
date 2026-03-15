package dev.saraki.wofuf.modules.forum.useCases.posts.deletePost

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Use case for deleting a post
 */
@Service
class DeletePostUseCase(
    private val postRepo: PostRepo,
) : UseCase<DeletePostDto.Request, DeletePostDto.Response> {

    override fun execute(request: DeletePostDto.Request): Result<DeletePostDto.Response> {
        // 1. Validate post ID
        if (request.postId.isBlank()) {
            return DeletePostErrors.PostIdEmptyError()
        }

        // 2. Validate and create PostId
        val postIdOrError = PostId.create(UniqueEntityId(request.postId))
        if (postIdOrError.isFailure) {
            return DeletePostErrors.PostNotFoundError(request.postId)
        }
        val postId = postIdOrError.getOrThrow()

        // 3. Check if post exists
        val postExists = postRepo.exists(postId)
        if (!postExists) {
            return DeletePostErrors.PostNotFoundError(request.postId)
        }

        // 4. Delete the post
        try {
            postRepo.delete(postId)
        } catch (e: Exception) {
            return DeletePostErrors.DeleteFailedError(request.postId)
        }

        // 5. Return success response
        return Result.success(DeletePostDto.Response())
    }
}
