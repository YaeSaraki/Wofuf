package dev.saraki.wofuf.modules.forum.useCases.admin.posts.unfeaturePost

import dev.saraki.wofuf.modules.forum.domain.OperationType
import dev.saraki.wofuf.modules.forum.domain.valueObjects.MemberId
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PermissionPoint
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.infra.annotation.RequirePermission
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.modules.forum.infra.services.OperationLogService
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class UnfeaturePostUseCase(
    private val postRepo: PostRepo,
    private val operationLogService: OperationLogService,
) : UseCase<UnfeaturePostDto.Request, UnfeaturePostDto.Response> {

    @RequirePermission(PermissionPoint.POST_FEATURE, "Only users with POST_FEATURE permission can unfeature posts")
    override fun execute(request: UnfeaturePostDto.Request): Result<UnfeaturePostDto.Response> {
        if (request.postId.isBlank()) {
            return UnfeaturePostErrors.PostIdEmptyError()
        }

        if (request.operatorMemberId.isBlank()) {
            return UnfeaturePostErrors.InvalidOperatorError()
        }

        val postIdOrError = PostId.create(UniqueEntityId(request.postId))
        if (postIdOrError.isFailure) {
            return UnfeaturePostErrors.InvalidPostIdError(request.postId)
        }
        val postId = postIdOrError.getOrThrow()

        val operatorMemberIdOrError = MemberId.create(UniqueEntityId(request.operatorMemberId))
        if (operatorMemberIdOrError.isFailure) {
            return UnfeaturePostErrors.InvalidOperatorError()
        }
        val operatorMemberId = operatorMemberIdOrError.getOrThrow()

        val post = postRepo.findPostByPostId(postId)
            ?: return UnfeaturePostErrors.PostNotFoundError(request.postId)

        val unfeatureResult = post.unfeature()
        if (unfeatureResult.isFailure) {
            return UnfeaturePostErrors.UnfeatureFailedError(request.postId, unfeatureResult.exceptionOrThrow().message ?: "Unknown error")
        }

        try {
            postRepo.save(unfeatureResult.getOrThrow())

            // 记录操作日志
            operationLogService.logPostAction(
                operationType = OperationType.POST_UNFEATURE,
                postId = request.postId,
                operatorId = operatorMemberId,
                details = "Unfeatured post: ${post.title}"
            )
        } catch (e: Exception) {
            return UnfeaturePostErrors.SaveFailedError(request.postId)
        }

        return Result.success(UnfeaturePostDto.Response(postId = request.postId, isFeatured = false))
    }
}
