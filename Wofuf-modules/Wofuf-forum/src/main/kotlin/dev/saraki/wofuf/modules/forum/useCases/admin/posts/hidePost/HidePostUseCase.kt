package dev.saraki.wofuf.modules.forum.useCases.admin.posts.hidePost

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
class HidePostUseCase(
    private val postRepo: PostRepo,
    private val operationLogService: OperationLogService,
) : UseCase<HidePostDto.Request, HidePostDto.Response> {

    @RequirePermission(PermissionPoint.POST_HIDE, "Only users with POST_HIDE permission can hide posts")
    override fun execute(request: HidePostDto.Request): Result<HidePostDto.Response> {
        if (request.postId.isBlank()) {
            return HidePostErrors.PostIdEmptyError()
        }

        if (request.hiddenByMemberId.isBlank()) {
            return HidePostErrors.HiddenByMemberIdEmptyError()
        }

        val postIdOrError = PostId.create(UniqueEntityId(request.postId))
        if (postIdOrError.isFailure) {
            return HidePostErrors.InvalidPostIdError(request.postId)
        }
        val postId = postIdOrError.getOrThrow()

        val hiddenByMemberIdOrError = MemberId.create(UniqueEntityId(request.hiddenByMemberId))
        if (hiddenByMemberIdOrError.isFailure) {
            return HidePostErrors.InvalidMemberIdError(request.hiddenByMemberId)
        }
        val hiddenByMemberId = hiddenByMemberIdOrError.getOrThrow()

        val post = postRepo.findPostByPostId(postId)
            ?: return HidePostErrors.PostNotFoundError(request.postId)

        val hideResult = post.hide(hiddenByMemberId)
        if (hideResult.isFailure) {
            return HidePostErrors.HideFailedError(request.postId, hideResult.exceptionOrThrow().message ?: "Unknown error")
        }

        try {
            postRepo.save(hideResult.getOrThrow())
        } catch (e: Exception) {
            return HidePostErrors.SaveFailedError(request.postId)
        }

        // 记录操作日志
        operationLogService.logPostAction(
            operationType = OperationType.POST_HIDE,
            postId = request.postId,
            operatorId = hiddenByMemberId,
            details = "Hidden post: ${post.title}"
        )

        return Result.success(HidePostDto.Response(postId = request.postId, status = "HIDDEN", isHidden = true))
    }
}
