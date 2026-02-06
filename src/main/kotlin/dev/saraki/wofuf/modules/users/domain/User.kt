package dev.saraki.wofuf.modules.users.domain

import dev.saraki.wofuf.modules.users.domain.events.UserCreated
import dev.saraki.wofuf.modules.users.domain.events.UserDeleted
import dev.saraki.wofuf.modules.users.domain.events.UserLoggedIn
import dev.saraki.wofuf.modules.users.useCases.deleteUser.DeleteUserErrors
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.AggregateRoot
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import java.time.LocalDateTime
import kotlin.Boolean

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:15
 *   @description:
 */
data class UserProps(
    val email: UserEmail,
    val username: UserName,
    val password: UserPassword,
    val isEmailVerified: Boolean? = false,
    val isAdminUser: Boolean? = false,
    var accessToken: JwtToken? = null,
    var refreshToken: JwtToken? = null,
    var isDeleted: Boolean? = false,
    val lastLogin: LocalDateTime? = null,
)

class User private constructor(
        props: UserProps,
        id: UniqueEntityId? = null
): AggregateRoot<UserProps>(props, id) {

    val userId: UserId
        get() = UserId.create(_id).getOrThrow()

    val email: UserEmail
        get() = props.email

    val username: UserName
        get() = props.username

    val password: UserPassword
        get() = props.password

    val accessToken: JwtToken?
        get() = props.accessToken

    val refreshToken: JwtToken?
        get() = props.refreshToken

    val isDeleted: Boolean
        get() = props.isDeleted ?: false

    val isAdminUser: Boolean
        get() = props.isAdminUser ?: false

    val lastLogin: LocalDateTime?
        get() = props.lastLogin

    val isEmailVerified: Boolean
        get() = props.isEmailVerified ?: false

    fun delete(): Result<Unit> {
        this.isDeleted.let {
            if (!it) {
                addDomainEvent(UserDeleted(this, LocalDateTime.now()))
                this.props.isDeleted = true
                return Result.success(Unit)
            }
        }
        return DeleteUserErrors.UserDeleteError(this.userId.stringValue)
    }

    fun setAccessToken(accessToken: String, refreshToken: String) {
        addDomainEvent(UserLoggedIn(this, LocalDateTime.now()))
        this.props.accessToken = accessToken
        this.props.refreshToken = refreshToken
    }

    companion object {
        fun create(props: UserProps, id: UniqueEntityId? = null): Result<User> {
            val guardResult = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(props.username, "username"),
                    Guard.GuardArgument(props.email, "email")
                )
            )

            // 校验失败，返回失败Result
            if (guardResult.isFailure) {
                return Result.failure(guardResult.exceptionOrThrow())
            }

            // 判断是否为新用户：id为空则为新创建
            val isNewUser = id == null

            val defaultProps = props.copy(
                isDeleted = props.isDeleted ?: false,
                isEmailVerified = props.isEmailVerified ?: false,
                isAdminUser = props.isAdminUser ?: false
            )

            val user = User(defaultProps, id)

            if (isNewUser) {
                user.addDomainEvent(UserCreated(user))
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