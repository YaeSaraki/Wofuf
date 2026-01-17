package dev.saraki.wofuf.modules.users.domain

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 23:16
 *   @description: 用户邮箱值对象
 */

import dev.saraki.wofuf.modules.users.useCases.createUser.CreateUserErrors
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.ValueObject

class UserEmail(val value: String) : ValueObject<UserEmail>() {
    companion object {
        private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@(.+)$")

        fun create(email: String): Result<UserEmail> {
            val trimmedEmail = format(email)
            if (!this.isValidEmail(trimmedEmail)) {
                return CreateUserErrors.EmailFormatError(trimmedEmail)

            }
            return Result.success(UserEmail(trimmedEmail))
        }

        fun isValidEmail(email: String): Boolean {
            return email.matches(emailRegex)
        }

        fun format(email: String): String {
            return email.trim().lowercase()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserEmail
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = value
}