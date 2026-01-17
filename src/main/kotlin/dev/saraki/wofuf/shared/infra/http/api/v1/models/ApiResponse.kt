package dev.saraki.wofuf.shared.infra.http.api.v1.models

import dev.saraki.wofuf.shared.core.AppError

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 22:34
 *   @description:
 */
data class ApiResponse<T> constructor(
    val success: Boolean,
    val message: String?,
    val data: T?,
    val error: String?
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(true, "Operation successful", data, null)
        }

        fun <T> success(message: String, data: T): ApiResponse<T> {
            return ApiResponse(true, message, data, null)
        }

        fun <T> error(error: String): ApiResponse<T> {
            return ApiResponse(false, null, null, error)
        }

        fun <T> error(error: AppError): ApiResponse<T> {
            return ApiResponse(false, error.message, null, error.code)
        }

        fun <T> error(message: String, error: String): ApiResponse<T> {
            return ApiResponse(false, message, null, error)
        }
    }
}