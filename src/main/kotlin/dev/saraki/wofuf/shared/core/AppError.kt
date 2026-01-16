package dev.saraki.wofuf.shared.core

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 11:18
 *   @description:
 */
open class AppError(
    message: String,
    val isOperational: Boolean = true
) : RuntimeException(message)