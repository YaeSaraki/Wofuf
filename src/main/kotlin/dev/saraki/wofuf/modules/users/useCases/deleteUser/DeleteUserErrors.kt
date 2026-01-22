package dev.saraki.wofuf.modules.users.useCases.deleteUser

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/17 16:32
 *   @description:
 */
class DeleteUserErrors {

    // 用户不存在错误
    class UserNotFoundError(id: String) : Result.Failure<Unit>(
        exception = UseCaseError(
            code = "USER_NOT_FOUND",
            message = "The user with id $id cannot be found"
        )
    )

    // 用户删除错误
    class UserDeleteError(id: String) : Result.Failure<Unit>(
        exception = UseCaseError(
            code = "USER_DELETE_ERROR",
            message = "The user with id $id cannot be deleted"
        )
    )

    // 未授权错误
    class UnauthorizedError : Result.Failure<Unit>(
        exception = UseCaseError(
            code = "UNAUTHORIZED",
            message = "The user is not authorized to delete this user"
        )
    )
}