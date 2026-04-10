package dev.saraki.wofuf.modules.forum.useCases.admin.posts.showPost

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class ShowPostUseCase(
    private val postRepo: PostRepo,
) : UseCase<ShowPostDto.Request, ShowPostDto.Response> {

    @RequirePermission(PermissionPoint.POST_HIDE, "Only users with POST_HIDE permission can show posts")
    override fun execute(request: ShowPostDto.Request): Result<ShowPostDto.Response> {
        if (request.postId.isBlank()) {
            return ShowPostErrors.PostIdEmptyError()
        }

        val postIdOrError = PostId.create(UniqueEntityId(request.postId))
        if (postIdOrError.isFailure) {
            return ShowPostErrors.InvalidPostIdError(request.postId)
        }
        val postId = postIdOrError.getOrThrow()

        val post = postRepo.findPostByPostId(postId)
            ?: return ShowPostErrors.PostNotFoundError(request.postId)

        val showResult = post.show()
        if (showResult.isFailure) {
            return ShowPostErrors.ShowFailedError(request.postId, showResult.exceptionOrThrow().message ?: "Unknown error")
        }

        try {
            postRepo.save(showResult.getOrThrow())
        } catch (e: Exception) {
            return ShowPostErrors.SaveFailedError(request.postId)
        }

        return Result.success(ShowPostDto.Response(postId = request.postId, status = "NORMAL", isHidden = false))
    }
}
