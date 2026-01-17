package dev.saraki.wofuf.modules.users.useCases.createUser

import dev.saraki.wofuf.modules.users.domain.UserEmail
import dev.saraki.wofuf.modules.users.domain.UserName
import dev.saraki.wofuf.modules.users.domain.UserPassword
import dev.saraki.wofuf.modules.users.dtos.UserDto
import dev.saraki.wofuf.shared.core.UseCaseError
import dev.saraki.wofuf.shared.core.Result

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/16 17:28
 *   @description:
 */
object CreateUserErrors {

    // ID已存在错误
    class IdAlreadyExistsError(id: String) : Result.Failure<UserDto>(
        exception = UseCaseError(
            code = "ID_ALREADY_EXISTS",
            message = "The id $id associated for this account already exists"
        )
    )

    // 邮箱已存在错误
    class EmailAlreadyExistsError(email: String) : Result.Failure<UserDto>(
        exception = UseCaseError(
            code = "EMAIL_ALREADY_EXISTS",
            message = "The email $email associated for this account already exists"
        )
    )

    // 用户名已存在错误
    class UsernameAlreadyExistsError(username: String) : Result.Failure<UserDto>(
        exception = UseCaseError(
            code = "USERNAME_ALREADY_EXISTS",
            message = "The username $username associated for this account already exists"
        )
    )

    // 用户名已被占用错误
    class UsernameTakenError(username: String) : Result.Failure<UserName>(
        exception = UseCaseError(
            code = "USERNAME_TAKEN",
            message = "The username $username was already taken"
        )
    )

    // 用户名格式错误
    class UsernameFormatError(username: String) : Result.Failure<UserName>(
        exception = UseCaseError(
            code = "USERNAME_FORMAT_ERROR",
            message = "The username $username is not in the correct format"
        )
    )

    // 邮箱格式错误
    class EmailFormatError(email: String) : Result.Failure<UserEmail>(
        exception = UseCaseError(
            code = "EMAIL_FORMAT_ERROR",
            message = "The email $email is not in the correct format"
        )
    )

    // 密码格式错误
    class PasswordFormatError(password: String) : Result.Failure<UserPassword>(
        exception = UseCaseError(
            code = "PASSWORD_FORMAT_ERROR",
            message = "The password $password is not in the correct format"
        )
    )

}
