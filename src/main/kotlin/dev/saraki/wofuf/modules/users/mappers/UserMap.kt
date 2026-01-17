package dev.saraki.wofuf.modules.users.mappers

import dev.saraki.wofuf.modules.users.domain.User
import dev.saraki.wofuf.modules.users.domain.UserEmail
import dev.saraki.wofuf.modules.users.domain.UserId
import dev.saraki.wofuf.modules.users.domain.UserName
import dev.saraki.wofuf.modules.users.domain.UserPassword
import dev.saraki.wofuf.modules.users.domain.UserProps
import dev.saraki.wofuf.modules.users.dtos.UserDto
import dev.saraki.wofuf.modules.users.repos.persistence.UserEntity
import dev.saraki.wofuf.shared.domain.UniqueEntityId

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
                accessToken = user.userProps.accessToken,
                isDeleted = user.userProps.isDeleted ?: false,
                password = user.userProps.password.value,
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
                accessToken = user.accessToken,
                isDeleted = user.isDeleted ?: false,
                password = user.password,
                lastLogin = user.lastLogin,
            )
        }
    }
}