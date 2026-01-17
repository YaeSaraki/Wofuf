package dev.saraki.wofuf.shared.core

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:37
 *   @description:
 */
open class UseCaseError(
    code: String,
    message: String,
) : AppError(message,  code) {
    override fun toString(): String = "UseCaseError(code=$code, message=${message})"
    fun getMessageOrDefault(default: String = "Unknown error"): String = message ?: default
}