package dev.saraki.wofuf.modules.users.dtos

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 23:17
 *   @description:
 */

import dev.saraki.wofuf.modules.users.domain.User
import java.time.LocalDateTime

class UserDto(
    val id: String,
    val email: String,
    val username: String,
    val isEmailVerified: Boolean,
    val isAdminUser: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun fromDomain(user: User): UserDto {
            return UserDto(
                id = user.id.toString(),
                email = user.email.value,
                username = user.username.value,
                isEmailVerified = user.isEmailVerified,
                isAdminUser = user.isAdminUser,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
}

class CreateUserRequest(
    val email: String,
    val username: String,
    val password: String
)

class LoginUserRequest(
    val email: String,
    val password: String
)

class UpdatePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)