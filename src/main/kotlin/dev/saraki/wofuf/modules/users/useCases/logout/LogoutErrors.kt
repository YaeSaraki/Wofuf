package dev.saraki.wofuf.modules.users.useCases.logout

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 11:07
 *   @description:
 */
class LogoutErrors {
    // 用户未找到错误
    class UserNotFoundError(username: String) : Result.Failure<LogoutDto.LogoutResponse>(
        exception = UseCaseError(
            code = "user_not_found",
            message = "User with username $username not found"
        )
    )

    // 令牌无效错误
    class InvalidTokenError : Result.Failure<LogoutDto.LogoutResponse>(
        exception = UseCaseError(
            code = "invalid_token",
            message = "Invalid or expired token"
        )
    )

}