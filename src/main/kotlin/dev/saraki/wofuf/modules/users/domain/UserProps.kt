package dev.saraki.wofuf.modules.users.domain

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
    val accessToken: JwtToken? = null,
    var isDeleted: Boolean? = false,
    val lastLogin: LocalDateTime? = null,
)
