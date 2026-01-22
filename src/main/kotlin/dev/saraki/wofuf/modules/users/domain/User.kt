package dev.saraki.wofuf.modules.users.domain

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:15
 *   @description:
 */


import dev.saraki.wofuf.modules.users.domain.events.UserCreated
import dev.saraki.wofuf.modules.users.domain.events.UserDeleted
import dev.saraki.wofuf.modules.users.domain.events.UserLoggedIn
import dev.saraki.wofuf.modules.users.useCases.deleteUser.DeleteUserErrors
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.AggregateRoot
import java.time.LocalDateTime
import kotlin.Boolean

class User: AggregateRoot<User> {
    val id: UserId
    val userProps: UserProps

    constructor(id: UserId, props: UserProps) {
        this.id = id
        this.userProps = props
    }

    fun delete(): Result<Unit> {
        userProps.isDeleted?.let {
            if (!it) {
                addDomainEvent(UserDeleted(this, LocalDateTime.now()))
                userProps.isDeleted = true
                return Result.success(Unit)
            }
        }
        return DeleteUserErrors.UserDeleteError(this.id.value.id)
    }

    fun setAccessToken(accessToken: String, refreshToken: String) {
        addDomainEvent(UserLoggedIn(this, LocalDateTime.now()))
        userProps.accessToken = accessToken
        userProps.refreshToken = refreshToken
    }

    companion object {
        fun create(props: UserProps, id: UserId?): Result<User> {
            val guardResult = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(props.username, "props"),
                    Guard.GuardArgument(id, "id")
                )
            )
            if (guardResult.isFailure) return Result.failure(guardResult.exceptionOrNull()!!)

            val user = User(
                props = props,
                id = id ?: UserId.create().getOrThrow()
            )

            if (id != null) {
                // 发布用户创建事件
                user.addDomainEvent(UserCreated(user, LocalDateTime.now()))
                return Result.success(user)
            }

            return Result.success(user)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as User
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}