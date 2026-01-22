package dev.saraki.wofuf.modules.users.useCases.login


/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 11:07
 *   @description:
 */
import dev.saraki.wofuf.modules.users.useCases.login.LoginDto.LoginResponse
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class LoginErrors {
    // 登录用户不存在
    class UserNotFoundError(username: String): Result.Failure<LoginResponse>(
        exception =  UseCaseError(
            code = "user_not_found",
            message = "User with username $username not found"
        )
    )
    // 密码错误
    class PasswordNotMatchError(username: String): Result.Failure<LoginResponse>(
        exception = UseCaseError(
            code = "password_not_match",
            message = "Password not match for user $username"
        )
    )
}