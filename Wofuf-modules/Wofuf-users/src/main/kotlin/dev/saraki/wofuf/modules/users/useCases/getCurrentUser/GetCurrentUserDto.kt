package dev.saraki.wofuf.modules.users.useCases.getCurrentUser

import java.time.LocalDateTime

class GetCurrentUserDto {
    data class Request(
        val userId: String,
    )

    data class Response(
        val userId: String,
        val username: String,
        val email: String,
        val isEmailVerified: Boolean,
        val isAdminUser: Boolean,
        val lastLogin: LocalDateTime?,
    )
}
