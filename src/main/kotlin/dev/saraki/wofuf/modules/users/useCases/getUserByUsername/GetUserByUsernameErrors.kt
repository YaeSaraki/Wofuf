package dev.saraki.wofuf.modules.users.useCases.getUserByUsername

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/17 21:53
 *   @description:
 */

import dev.saraki.wofuf.shared.core.UseCaseError
import dev.saraki.wofuf.shared.core.Result

class GetUserByUsernameErrors {
    // 用户不存在错误
    class UserNotFoundError(val username: String) : Result.Failure<GetUserByUsernameDto.GetUserResponse>(
        exception = UseCaseError(
            code = "USER_NOT_FOUND",
            message = "The user cannot be found"
        )
    )

    // 用户名无效错误
    class UserNameInvalidError(val username: String) : Result.Failure<GetUserByUsernameDto.GetUserResponse>(
        exception = UseCaseError(
            code = "USER_NAME_INVALID",
            message = "The username is invalid"
        )
    )
}