package dev.saraki.wofuf.modules.users.domain.valueObjects

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 23:17
 *   @description: 用户名值对象
 */

import dev.saraki.wofuf.modules.users.useCases.createUser.CreateUserErrors
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.ValueObject

data class UserNameProps(val value: String)

class UserName private constructor(props: UserNameProps) : ValueObject<UserNameProps>(props) {
    val value: String get() = props.value

    companion object {
        val UNKNOWN: UserName
            get() = UserName(UserNameProps("UNKNOWN"))

        fun create(userName: String): Result<UserName> {
            val trimmedUserName = userName.trim()

            val validation = Guard.combine(
                listOf(
                    Guard.againstNullOrUndefined(trimmedUserName, "userName"),
                    Guard.againstAtLeast(3, "userName"),
                    Guard.againstAtMost(50, "userName")
                )
            )

            if (validation.isFailure) {
                return Result.failure(validation.exceptionOrThrow())
            }

            // 检查用户名是否只包含允许的字符
            if (!trimmedUserName.matches(Regex("^[a-zA-Z0-9_-]+$"))) {
                return Result.failure("userName format error, only allow a-z A-Z 0-9 _ -, but your userName is $trimmedUserName")
            }

            return Result.success(UserName(UserNameProps(trimmedUserName)))
        }
    }
}