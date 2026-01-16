package dev.saraki.wofuf.modules.users.domain

import java.time.LocalDateTime

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/15 18:51
 *   @description:
 */
interface UserProps {
    val email: UserEmail
    val username: UserName
    val password: UserPassword
    val isEmailVerified: Boolean
    val isAdminUser: Boolean
    val createdAt: LocalDateTime
    val updatedAt: LocalDateTime
}