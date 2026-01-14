package dev.saraki.wofuf.shared.core

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 11:20
 *   @description:
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val error: AppError) : Result<Nothing>()

    companion object {
        fun <T> success(data: T): Result<T> = Success(data)
        fun failure(error: AppError): Result<Nothing> = Failure(error)
    }
}