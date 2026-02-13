package dev.saraki.wofuf.modules.users.infra.repos.jpa.mappers

import dev.saraki.wofuf.modules.users.domain.User
import dev.saraki.wofuf.modules.users.domain.UserProps
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserEmail
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserName
import dev.saraki.wofuf.modules.users.domain.valueObjects.UserPassword
import dev.saraki.wofuf.modules.users.infra.repos.jpa.entities.UserEntity
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.domain.UniqueEntityId

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/8 16:55
 *   @description:
 */
object UserEntityMapper {
    fun toDomain(entity: UserEntity): User {
        val guardResult = Guard.againstNullOrUndefinedBulk(
            listOf(
                Guard.GuardArgument(entity.userId, "userId"),
            )
        )
        if (guardResult.isFailure) {
            throw guardResult.exceptionOrThrow()
        }

        val userOrError = User.create(
            props = UserProps(
                email = UserEmail.create(entity.email).getOrThrow(),
                name = UserName.create(entity.userName).getOrThrow(),
                isEmailVerified = entity.isEmailVerified,
                isAdminUser = entity.isAdminUser,
                isDeleted = entity.isDeleted,
                password = UserPassword.create(entity.password, true).getOrThrow(),
                lastLogin = entity.lastLogin,
            ),
            id = UniqueEntityId(entity.userId)
        )
        if (userOrError.isFailure) {
            throw userOrError.exceptionOrThrow()
        }
        val user = userOrError.getOrThrow()

        user._createdAt = entity.createdAt
        user._updatedAt = entity.updatedAt

        return user
    }

    fun toEntity(user: User): UserEntity {
        val entity = UserEntity(
            userId = user.userId.stringValue,
            userName = user.username.value,
            password = user.password.value,
            email = user.email.value,
            isEmailVerified = user.isEmailVerified,
            isAdminUser = user.isAdminUser,
            isDeleted = user.isDeleted,
            lastLogin = user.lastLogin,
        )
        return entity
    }
}