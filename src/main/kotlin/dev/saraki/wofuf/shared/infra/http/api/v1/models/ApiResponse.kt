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
    val code: String?,
    val data: T?,
    val message: String?
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(true, "Success" , data, "Operation successful")
        }

        fun <T> success(message: String, data: T): ApiResponse<T> {
            return ApiResponse(true, "Success", data, message)
        }

        fun <T> error(message: String): ApiResponse<T>  {
            return ApiResponse(false, "Error", null, message)
        }

        fun <T> error(error: AppError): ApiResponse<T> {
            return ApiResponse(false, error.code, null, error.message)
        }

    }
}