package dev.saraki.wofuf.shared.core

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:37
 *   @description:
 */
class UseCaseError(
    val code: String,
    message: String,
    isOperational: Boolean = true
) : AppError(message, isOperational) {
    override fun toString(): String = "UseCaseError(code=$code, message=${message})"
}