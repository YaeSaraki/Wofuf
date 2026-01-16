package dev.saraki.wofuf.modules.users.domain

import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.domain.ValueObject

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 23:16
 *   @description: 用户邮箱值对象
 */
class UserEmail private constructor(val value: String) : ValueObject<UserEmail>() {
    companion object {
        private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@(.+)$")

        fun create(email: String): Result<UserEmail> {
            val trimmedEmail = email.trim()

            val validation = Guard.combine(listOf(
                Guard.againstNullOrUndefined(trimmedEmail, "email"),
                Guard.againstAtLeast(3, trimmedEmail),
                Guard.againstAtMost(255, trimmedEmail)
            ))

            if (validation.isFailure) {
                return Result.failure(validation.exceptionOrNull()!!)
            }

            if (!emailRegex.matches(trimmedEmail)) {
                return Result.failure(Exception("Invalid email format"))
            }

            return Result.success(UserEmail(trimmedEmail.lowercase()))
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