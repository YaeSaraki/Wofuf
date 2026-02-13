package dev.saraki.wofuf.modules.users.domain.valueObjects

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 23:17
 *   @description: 用户密码值对象
 */

import dev.saraki.wofuf.auth.config.PasswordEncoder
import dev.saraki.wofuf.modules.users.useCases.createUser.CreateUserErrors
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.ValueObject


data class UserPasswordProps(val value: String)

class UserPassword private constructor(props: UserPasswordProps) : ValueObject<UserPasswordProps>(props) {
    val value: String get() = props.value

    companion object {
        val passwordEncoder = PasswordEncoder()

        fun create(password: String, hash: Boolean): Result<UserPassword> {
            val validation = Guard.combine(
                listOf(
                    Guard.againstNullOrUndefined(password, "password"),
                    Guard.againstAtLeast(6, password),
                    Guard.againstAtMost(100, password)
                )
            )

            if (validation.isFailure) {
                return CreateUserErrors.PasswordFormatError(password)
            }

            if (!hash) {
                val hashedPassword = hashPassword(password)
                return Result.success(UserPassword(UserPasswordProps(hashedPassword)))
            }

            return Result.success(UserPassword(UserPasswordProps(password)))
        }

        private fun hashPassword(password: String): String {
            return passwordEncoder.encode(password)!!
        }


    }

    fun matches(plainPassword: String): Boolean {
        return passwordEncoder.matches(plainPassword, value)
    }

    fun getHashedValue(): String = value
}

