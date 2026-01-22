package dev.saraki.wofuf.modules.users.dtos

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 23:17
 *   @description:
 */

import dev.saraki.wofuf.modules.users.domain.User
import dev.saraki.wofuf.modules.users.domain.UserEmail
import dev.saraki.wofuf.modules.users.domain.UserId
import dev.saraki.wofuf.modules.users.domain.UserName
import dev.saraki.wofuf.modules.users.domain.UserPassword
import dev.saraki.wofuf.modules.users.domain.UserProps
import dev.saraki.wofuf.modules.users.mappers.UserMap
import dev.saraki.wofuf.modules.users.infra.repos.entities.UserEntity
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import java.io.Serializable
import java.time.LocalDateTime

data class UserDto(
    val id: String,
    val email: String,
    val userName: String,
    val password: String,
    val isEmailVerified: Boolean,
    val isAdminUser: Boolean,
    val isDeleted: Boolean,
    val lastLogin: LocalDateTime?,
) : UserMap(), Serializable {
    override fun toEntity(): UserEntity {
        return toEntity(this)
    }
    override fun toDomain(): User {
        return toDomain(this)
    }
    fun toEntity(userDto: UserDto): UserEntity {
        return UserEntity(
            id = userDto.id,
            email = userDto.email,
            username = userDto.userName,
            isEmailVerified = userDto.isEmailVerified,
            isAdminUser = userDto.isAdminUser,
            isDeleted = userDto.isDeleted,
            password = UserPassword(userDto.password).getHashedValue(),
            lastLogin = userDto.lastLogin,
        )
    }
    fun toDomain(userDto: UserDto): User {
        return User(
            props = UserProps(
                email = UserEmail.create(userDto.email).getOrThrow(),
                username = UserName.create(userDto.userName).getOrThrow(),
                isEmailVerified = userDto.isEmailVerified,
                isAdminUser = userDto.isAdminUser,
                isDeleted = userDto.isDeleted,
                password = UserPassword.create(userDto.password).getOrThrow(),
                lastLogin = userDto.lastLogin,
            ),
            id = UserId(UniqueEntityId(userDto.id)),
        )
    }

}
//
//class LoginUserRequest(
//    val email: String,
//    val password: String
//)
//
//class UpdatePasswordRequest(
//    val currentPassword: String,
//    val newPassword: String
//)