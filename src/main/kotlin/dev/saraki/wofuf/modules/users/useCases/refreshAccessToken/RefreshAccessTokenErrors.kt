package dev.saraki.wofuf.modules.users.useCases.refreshAccessToken


/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 11:07
 *   @description:
 */

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class RefreshAccessTokenErrors {
    // 用户未找到错误
    class UserNotFoundError(username: String) : Result.Failure<RefreshAccessTokenDto.RefreshAccessTokenResponse>(
        exception = UseCaseError(
            code = "user_not_found",
            message = "User with username $username not found"
        )
    )

    // 令牌无效错误
    class InvalidTokenError : Result.Failure<RefreshAccessTokenDto.RefreshAccessTokenResponse>(
        exception = UseCaseError(
            code = "invalid_token",
            message = "Invalid or expired token"
        )
    )

    // 刷新令牌不存在错误
    class RefreshTokenNotFoundError : Result.Failure<RefreshAccessTokenDto.RefreshAccessTokenResponse>(
        exception = UseCaseError(
            code = "refresh_token_not_found",
            message = "Refresh token not found"
        )
    )
}