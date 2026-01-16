package dev.saraki.wofuf.modules.users.domain

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:15
 *   @description:
 */


import dev.saraki.wofuf.modules.users.domain.events.UserCreated
import dev.saraki.wofuf.modules.users.domain.events.UserLoggedIn
import java.time.LocalDateTime

class User private constructor(
    val id: UserId,
    private val props: UserProps
) : UserProps by props {

    companion object {
        fun create(
            email: UserEmail,
            username: UserName,
            password: UserPassword,
            isAdmin: Boolean = false
        ): Result<User> {
            val now = LocalDateTime.now()
            val user = User(
                id = UserId.create().getOrThrow(),
                props = object : UserProps {
                    override val email: UserEmail = email
                    override val username: UserName = username
                    override val password: UserPassword = password
                    override val isEmailVerified: Boolean = false
                    override val isAdminUser: Boolean = isAdmin
                    override val createdAt: LocalDateTime = now
                    override val updatedAt: LocalDateTime = now
                }
            )

            // 发布用户创建事件
            UserCreated(user.id, user.email, user.username, now)

            return Result.success(user)
        }

        fun fromExisting(
            id: UserId,
            email: UserEmail,
            username: UserName,
            password: UserPassword,
            isEmailVerified: Boolean,
            isAdminUser: Boolean,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime
        ): User {
            return User(
                id = id,
                props = object : UserProps {
                    override val email: UserEmail = email
                    override val username: UserName = username
                    override val password: UserPassword = password
                    override val isEmailVerified: Boolean = isEmailVerified
                    override val isAdminUser: Boolean = isAdminUser
                    override val createdAt: LocalDateTime = createdAt
                    override val updatedAt: LocalDateTime = updatedAt
                }
            )
        }
    }

    fun verifyEmail(): User {
        return User(
            id = this.id,
            props = object : UserProps {
                override val email: UserEmail = this@User.email
                override val username: UserName = this@User.username
                override val password: UserPassword = this@User.password
                override val isEmailVerified: Boolean = true
                override val isAdminUser: Boolean = this@User.isAdminUser
                override val createdAt: LocalDateTime = this@User.createdAt
                override val updatedAt: LocalDateTime = LocalDateTime.now()
            }
        )
    }

    fun updatePassword(newPassword: UserPassword): User {
        return User(
            id = this.id,
            props = object : UserProps {
                override val email: UserEmail = this@User.email
                override val username: UserName = this@User.username
                override val password: UserPassword = newPassword
                override val isEmailVerified: Boolean = this@User.isEmailVerified
                override val isAdminUser: Boolean = this@User.isAdminUser
                override val createdAt: LocalDateTime = this@User.createdAt
                override val updatedAt: LocalDateTime = LocalDateTime.now()
            }
        )
    }

    fun login(): User {
        UserLoggedIn(this.id, this.email, LocalDateTime.now())
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as User
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}