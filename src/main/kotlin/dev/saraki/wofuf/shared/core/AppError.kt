package dev.saraki.wofuf.shared.core

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 11:18
 *   @description:
 */
open class AppError(
    override val message: String,
    val code: String = "UNKNOWN_ERROR",
) : RuntimeException(message)