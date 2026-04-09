package dev.saraki.wofuf.modules.forum.useCases.admin.posts.setPostUnderReview

import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class SetPostUnderReviewUseCase(
    private val postRepo: PostRepo,
) : UseCase<SetPostUnderReviewDto.Request, SetPostUnderReviewDto.Response> {

    @RequirePermission(PermissionPoint.POST_REVIEW, "Only users with POST_REVIEW permission can set posts under review")
    override fun execute(request: SetPostUnderReviewDto.Request): Result<SetPostUnderReviewDto.Response> {
        if (request.postId.isBlank()) {
            return SetPostUnderReviewErrors.PostIdEmptyError()
        }

        val postIdOrError = PostId.create(UniqueEntityId(request.postId))
        if (postIdOrError.isFailure) {
            return SetPostUnderReviewErrors.InvalidPostIdError(request.postId)
        }
        val postId = postIdOrError.getOrThrow()

        val post = postRepo.findPostByPostId(postId)
            ?: return SetPostUnderReviewErrors.PostNotFoundError(request.postId)

        val reviewResult = post.setUnderReview()
        if (reviewResult.isFailure) {
            return SetPostUnderReviewErrors.ReviewFailedError(request.postId, reviewResult.exceptionOrThrow().message ?: "Unknown error")
        }

        try {
            postRepo.save(reviewResult.getOrThrow())
        } catch (e: Exception) {
            return SetPostUnderReviewErrors.SaveFailedError(request.postId)
        }

        return Result.success(SetPostUnderReviewDto.Response(postId = request.postId, status = "UNDER_REVIEW"))
    }
}
