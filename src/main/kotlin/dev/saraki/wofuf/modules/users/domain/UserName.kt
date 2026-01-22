package dev.saraki.wofuf.modules.users.domain

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

class UserName(val value: String) : ValueObject<UserName>() {
    companion object {
        fun create(userName: String): Result<UserName> {
            val trimmedUserName = userName.trim()

            val validation = Guard.combine(listOf(
                Guard.againstNullOrUndefined(trimmedUserName, "userName"),
                Guard.againstAtLeast(3, trimmedUserName),
                Guard.againstAtMost(50, trimmedUserName)
            ))

            if (validation.isFailure) {
                return Result.failure(validation.exceptionOrThrow())
            }

            // 检查用户名是否只包含允许的字符
            if (!trimmedUserName.matches(Regex("^[a-zA-Z0-9_-]+$"))) {
                return CreateUserErrors.UsernameFormatError(trimmedUserName)
            }

            return Result.success(UserName(trimmedUserName))
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserName
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = value
}