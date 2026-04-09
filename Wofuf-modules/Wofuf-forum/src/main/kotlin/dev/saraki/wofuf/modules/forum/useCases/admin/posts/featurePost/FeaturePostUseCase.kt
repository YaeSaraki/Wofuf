package dev.saraki.wofuf.modules.forum.useCases.admin.posts.featurePost

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class FeaturePostUseCase(
    private val postRepo: PostRepo,
) : UseCase<FeaturePostDto.Request, FeaturePostDto.Response> {

    @RequirePermission(PermissionPoint.POST_FEATURE, "Only users with POST_FEATURE permission can feature posts")
    override fun execute(request: FeaturePostDto.Request): Result<FeaturePostDto.Response> {
        if (request.postId.isBlank()) {
            return FeaturePostErrors.PostIdEmptyError()
        }

        val postIdOrError = PostId.create(UniqueEntityId(request.postId))
        if (postIdOrError.isFailure) {
            return FeaturePostErrors.InvalidPostIdError(request.postId)
        }
        val postId = postIdOrError.getOrThrow()

        val post = postRepo.findPostByPostId(postId)
            ?: return FeaturePostErrors.PostNotFoundError(request.postId)

        val featureResult = post.feature()
        if (featureResult.isFailure) {
            return FeaturePostErrors.FeatureFailedError(request.postId, featureResult.exceptionOrThrow().message ?: "Unknown error")
        }

        try {
            postRepo.save(featureResult.getOrThrow())
        } catch (e: Exception) {
            return FeaturePostErrors.SaveFailedError(request.postId)
        }

        return Result.success(FeaturePostDto.Response(postId = request.postId, isFeatured = true))
    }
}
