package dev.saraki.wofuf.modules.users.domain.valueObjects

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 23:16
 *   @description: 用户邮箱值对象
 */

import dev.saraki.wofuf.modules.users.useCases.createUser.CreateUserErrors
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.ValueObject

data class UserEmailProps(val value: String)

class UserEmail private constructor(props: UserEmailProps) : ValueObject<UserEmailProps>(props) {
    val value: String get() = props.value

    companion object {
        private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@(.+)$")

        fun create(email: String): Result<UserEmail> {
            val trimmedEmail = format(email)
            if (!this.isValidEmail(trimmedEmail)) {
                return CreateUserErrors.EmailFormatError(trimmedEmail)

            }
            return Result.success(UserEmail(UserEmailProps(trimmedEmail)))
        }

        fun isValidEmail(email: String): Boolean {
            return email.matches(emailRegex)
        }

        fun format(email: String): String {
            return email.trim().lowercase()
        }
    }
}