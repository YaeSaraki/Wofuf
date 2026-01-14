package dev.saraki.wofuf.shared.core

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 11:18
 *   @description:
 */
sealed class AppError {
    data class NotFound(val message: String) : AppError()
    data class ValidationError(val message: String) : AppError()
    data class Unauthorized(val message: String) : AppError()
    data class Unknown(val message: String) : AppError()
}