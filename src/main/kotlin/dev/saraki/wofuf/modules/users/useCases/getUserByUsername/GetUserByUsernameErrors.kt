package dev.saraki.wofuf.modules.users.useCases.getUserByUsername

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/17 21:53
 *   @description:
 */

import dev.saraki.wofuf.modules.users.dtos.UserDto
import dev.saraki.wofuf.shared.core.UseCaseError
import dev.saraki.wofuf.shared.core.Result

class GetUserByUsernameErrors {
    // 用户不存在错误
    class UserNotFoundError(val username: String) : Result.Failure<UserDto>(
        exception = UseCaseError(
            code = "USER_NOT_FOUND",
            message = "The user cannot be found"
        )
    )
}