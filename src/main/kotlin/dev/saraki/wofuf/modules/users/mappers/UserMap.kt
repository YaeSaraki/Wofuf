package dev.saraki.wofuf.modules.users.mappers

import dev.saraki.wofuf.modules.users.domain.User
import dev.saraki.wofuf.modules.users.dtos.UserDto
import dev.saraki.wofuf.modules.users.infra.repos.entities.UserEntity

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/17 00:55
 *   @description:
 */
abstract class UserMap {
    abstract fun toEntity(): UserEntity
    abstract fun toDomain(): User

    companion object {
        fun from(user: User): UserDto {
            return UserDto(
                id = user.userId.stringValue,
                email = user.email.value,
                userName = user.username.value,
                isEmailVerified = user.isEmailVerified,
                isAdminUser = user.isAdminUser,
                isDeleted = user.isDeleted,
                password = user.password.getHashedValue(),
                lastLogin = user.lastLogin,
            )
        }

        fun from(user: UserEntity): UserDto {
            return UserDto(
                id = user.id,
                email = user.email,
                userName = user.username,
                isEmailVerified = user.isEmailVerified,
                isAdminUser = user.isAdminUser,
                isDeleted = user.isDeleted ?: false,
                password = user.password,
                lastLogin = user.lastLogin,
            )
        }
    }
}