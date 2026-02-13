package dev.saraki.wofuf.modules.users.domain

import dev.saraki.wofuf.modules.users.domain.events.CreateUser
import dev.saraki.wofuf.modules.users.domain.events.UserCreated
import dev.saraki.wofuf.modules.users.domain.events.UserDeleted
import dev.saraki.wofuf.modules.users.domain.events.UserLoggedIn
import dev.saraki.wofuf.modules.users.domain.valueObjects.*
import dev.saraki.wofuf.modules.users.useCases.deleteUser.DeleteUserErrors
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.domain.AggregateRoot
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:15
 *   @description:
 */
data class UserProps(
    val email: UserEmail,
    val name: UserName,
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
    id: UniqueEntityId?
) : AggregateRoot<UserProps>(props, id), UserDetails {

    val userId: UserId
        get() = UserId.Companion.create(_id).getOrThrow()

    val email: UserEmail
        get() = props.email

    val username: UserName
        get() = props.name

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
                addDomainEvent(UserDeleted(this))
                this.props.isDeleted = true
                return Result.success(Unit)
            }
        }
        return DeleteUserErrors.UserDeleteError(this.userId.stringValue)
    }


    fun setAccessToken(accessToken: String, refreshToken: String) {
        addDomainEvent(UserLoggedIn(this))
        this.props.accessToken = accessToken
        this.props.refreshToken = refreshToken
    }

    companion object {
        fun create(props: UserProps, id: UniqueEntityId? = null): Result<User> {
            val guardResult = Guard.againstNullOrUndefinedBulk(
                listOf(
                    Guard.GuardArgument(props.name, "username"),
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
                user.addDomainEvent(CreateUser(user.userId.stringValue, user.username.value, user.password.value))
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
    override fun getAuthorities(): Collection<GrantedAuthority> {
        return emptyList()
    }

    override fun getPassword(): String? {
        return password.value
    }

    override fun getUsername(): String {
        return username.value
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return !this.isDeleted
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }
    override fun isEnabled(): Boolean {
        return !this.isDeleted
    }

    override fun toString(): String {
        return "User(userId=$userId, email=$email, username=$username, password=$password, accessToken=$accessToken, refreshToken=$refreshToken, isDeleted=$isDeleted, isAdminUser=$isAdminUser, lastLogin=$lastLogin, isEmailVerified=$isEmailVerified)"
    }

}