package dev.saraki.wofuf.modules.forum.useCases.posts.deletePost

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 * @author YaeSaraki
 * @email ikaraswork@iCloud.com
 * @date 2026/3/15
 * @description Error classes for delete post use case
 */
class DeletePostErrors {

    /** Post ID is empty */
    class PostIdEmptyError : Result.Failure<DeletePostDto.Response>(
        exception = UseCaseError(
            code = "POST_ID_EMPTY_ERROR",
            message = "Post ID cannot be empty"
        )
    )

    /** Post not found */
    class PostNotFoundError(val postId: String) : Result.Failure<DeletePostDto.Response>(
        exception = UseCaseError(
            code = "POST_NOT_FOUND_ERROR",
            message = "Couldn't find a post by postId {$postId}"
        )
    )

    /** Delete failed */
    class DeleteFailedError(val postId: String) : Result.Failure<DeletePostDto.Response>(
        exception = UseCaseError(
            code = "DELETE_FAILED_ERROR",
            message = "Failed to delete post with postId {$postId}"
        )
    )

    /** User not authenticated */
    class UnauthorizedError : Result.Failure<DeletePostDto.Response>(
        exception = UseCaseError(
            code = "UNAUTHORIZED_ERROR",
            message = "用户未登录"
        )
    )

    /** Member not found */
    class MemberNotFoundError : Result.Failure<DeletePostDto.Response>(
        exception = UseCaseError(
            code = "MEMBER_NOT_FOUND_ERROR",
            message = "用户信息不存在"
        )
    )

    /** Forbidden - not the author */
    class ForbiddenError : Result.Failure<DeletePostDto.Response>(
        exception = UseCaseError(
            code = "FORBIDDEN_ERROR",
            message = "无权删除此帖子"
        )
    )
}
