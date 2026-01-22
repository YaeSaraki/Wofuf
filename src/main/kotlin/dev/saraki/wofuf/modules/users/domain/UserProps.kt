package dev.saraki.wofuf.modules.users.domain

import dev.saraki.wofuf.shared.domain.ValueObject
import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/15 18:51
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
) : ValueObject<UserProps>() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserProps
        return email == other.email && username == other.username && password == other.password && isEmailVerified == other.isEmailVerified && isAdminUser == other.isAdminUser && accessToken == other.accessToken && refreshToken == other.refreshToken && isDeleted == other.isDeleted && lastLogin == other.lastLogin
    }

    override fun hashCode(): Int {
        return email.hashCode() * 31 + username.hashCode() * 31 + password.hashCode() * 31 + isEmailVerified.hashCode() * 31 + isAdminUser.hashCode() * 31 + accessToken.hashCode() * 31 + refreshToken.hashCode() * 31 + isDeleted.hashCode() * 31 + lastLogin.hashCode() * 31
    }

    override fun toString(): String {
        return "UserProps(email=$email, username=$username, password=$password, isEmailVerified=$isEmailVerified, isAdminUser=$isAdminUser, accessToken=$accessToken, refreshToken=$refreshToken, isDeleted=$isDeleted, lastLogin=$lastLogin)"
    }
}
