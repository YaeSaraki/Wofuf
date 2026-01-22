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
                id = user.id.value.id,
                email = user.userProps.email.value,
                userName = user.userProps.username.value,
                isEmailVerified = user.userProps.isEmailVerified ?: false,
                isAdminUser = user.userProps.isAdminUser ?: false,
                isDeleted = user.userProps.isDeleted ?: false,
                password = user.userProps.password.getHashedValue(),
                lastLogin = user.userProps.lastLogin,
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