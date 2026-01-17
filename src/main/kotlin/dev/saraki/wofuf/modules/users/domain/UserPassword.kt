package dev.saraki.wofuf.modules.users.domain

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 23:17
 *   @description: 用户密码值对象
 */

import dev.saraki.wofuf.modules.users.useCases.createUser.CreateUserErrors
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.ValueObject
import java.security.MessageDigest
import java.util.Base64


class UserPassword(val value: String) : ValueObject<UserPassword>() {
    companion object {
        private val digest = MessageDigest.getInstance("SHA-256")

        fun create(password: String): Result<UserPassword> {
            val validation = Guard.combine(listOf(
                Guard.againstNullOrUndefined(password, "password"),
                Guard.againstAtLeast(6, password),
                Guard.againstAtMost(100, password)
            ))

            if (validation.isFailure) {
                return CreateUserErrors.PasswordFormatError(password)
            }

            val hashedPassword = hashPassword(password)
            return Result.success(UserPassword(hashedPassword))
        }

        private fun hashPassword(password: String): String {
            val hash = digest.digest(password.toByteArray())
            return Base64.getEncoder().encodeToString(hash)
        }
    }

    fun matches(plainPassword: String): Boolean {
        return value == hashPassword(plainPassword)
    }

    fun getHashedValue(): String = value

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserPassword
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()
    override fun toString(): String = value
}

