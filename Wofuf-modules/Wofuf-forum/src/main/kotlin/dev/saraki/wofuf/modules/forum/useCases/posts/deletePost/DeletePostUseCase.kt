package dev.saraki.wofuf.modules.forum.useCases.posts.deletePost

import dev.saraki.wofuf.auth.infra.JwtAuthFilter
import dev.saraki.wofuf.modules.forum.domain.valueObjects.PostId
import dev.saraki.wofuf.modules.forum.infra.repos.MemberRepo
import dev.saraki.wofuf.modules.forum.infra.repos.PostRepo
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserId
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
    private val memberRepo: MemberRepo,
) : UseCase<DeletePostDto.Request, DeletePostDto.Response> {

    override fun execute(request: DeletePostDto.Request): Result<DeletePostDto.Response> {
        // 1. 验证用户已登录
        if (request.currentUserId.isBlank()) {
            return DeletePostErrors.UnauthorizedError()
        }

        val userIdOrError = UserId.create(UniqueEntityId(request.currentUserId))
        if (userIdOrError.isFailure) {
            return DeletePostErrors.UnauthorizedError()
        }
        val userId = userIdOrError.getOrThrow()

        // 2. 验证 post ID
        if (request.postId.isBlank()) {
            return DeletePostErrors.PostIdEmptyError()
        }

        // 3. 验证并创建 PostId
        val postIdOrError = PostId.create(UniqueEntityId(request.postId))
        if (postIdOrError.isFailure) {
            return DeletePostErrors.PostNotFoundError(request.postId)
        }
        val postId = postIdOrError.getOrThrow()

        // 4. 查找帖子
        val post = postRepo.findPostByPostId(postId)
            ?: return DeletePostErrors.PostNotFoundError(request.postId)

        // 5. 获取当前用户的 member
        val member = memberRepo.findMemberByUserId(userId)
            ?: return DeletePostErrors.MemberNotFoundError()

        // 6. 检查权限：是帖子作者或管理员（JWT claim 来自 users 服务）
        val isAuthor = post.memberId.stringValue == member.memberId.stringValue
        val isAdmin = JwtAuthFilter.isAdmin()

        if (!isAuthor && !isAdmin) {
            return DeletePostErrors.ForbiddenError()
        }

        // 7. 删除帖子
        try {
            postRepo.delete(postId)
        } catch (e: Exception) {
            return DeletePostErrors.DeleteFailedError(request.postId)
        }

        // 8. 返回成功响应
        return Result.success(DeletePostDto.Response())
    }
}
