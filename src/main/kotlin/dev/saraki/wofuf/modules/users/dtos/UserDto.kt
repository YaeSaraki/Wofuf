package dev.saraki.wofuf.modules.users.dtos

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 23:17
 *   @description:
 */

import dev.saraki.wofuf.modules.users.domain.JwtToken
import dev.saraki.wofuf.modules.users.domain.User
import dev.saraki.wofuf.modules.users.domain.UserEmail
import dev.saraki.wofuf.modules.users.domain.UserId
import dev.saraki.wofuf.modules.users.domain.UserName
import dev.saraki.wofuf.modules.users.domain.UserPassword
import dev.saraki.wofuf.modules.users.domain.UserProps
import dev.saraki.wofuf.modules.users.mappers.UserMap
import dev.saraki.wofuf.modules.users.repos.persistence.UserEntity
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
    val accessToken: JwtToken?,
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
            accessToken = userDto.accessToken,
            isDeleted = userDto.isDeleted,
            password = UserPassword(userDto.password).value,
            lastLogin = userDto.lastLogin,
        )
    }
    fun toDomain(userDto: UserDto): User {
        return User(
            props = UserProps(
                email = UserEmail(userDto.email),
                username = UserName(userDto.userName),
                isEmailVerified = userDto.isEmailVerified,
                isAdminUser = userDto.isAdminUser,
                accessToken = userDto.accessToken,
                isDeleted = userDto.isDeleted,
                password = UserPassword(userDto.password),
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