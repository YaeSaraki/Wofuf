package dev.saraki.wofuf.modules.forum.infra.exception

import dev.saraki.wofuf.shared.core.AppError
import dev.saraki.wofuf.shared.core.UseCaseError
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Forum 模块全局异常处理
 * 将业务异常映射为正确的 HTTP 状态码
 *
 * 注意: 业务错误（如 Result.Failure 子类）由 Controller 直接处理，
 * 此处理器仅处理未被捕获的 Throwable 类型异常
 */
@RestControllerAdvice
class ForumExceptionHandler {

    @ExceptionHandler(UseCaseError::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleUseCaseError(error: UseCaseError): ApiResponse<Nothing> {
        return ApiResponse(false, error.code, null, error.message)
    }

    @ExceptionHandler(AppError::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleAppError(error: AppError): ApiResponse<Nothing> {
        return ApiResponse(false, error.code, null, error.message)
    }
}
