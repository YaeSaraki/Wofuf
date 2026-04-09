package dev.saraki.wofuf.modules.users.useCases.deleteUser

import dev.saraki.wofuf.modules.users.config.UserApiConstantV1
import dev.saraki.wofuf.modules.users.infra.security.requireCurrentUserId
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/17 16:31
 *   @description:
 */
@RestController
@RequestMapping(UserApiConstantV1.Base.ROOT)
class DeleteUserController(
    private val deleteUserUseCase: DeleteUserUseCase
) : BaseController() {
    /**
     * 删除用户
     * @param userId 用户ID（通过路径参数传递，符合 RESTful 规范）
     * @return 标准化响应，成功返回 204 No Content
     */
    @DeleteMapping(UserApiConstantV1.Base.BY_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(@PathVariable userId: String): ApiResponse<Unit> {
        val currentUserId = requireCurrentUserId()
        
        // 验证用户权限：只能删除自己的账户
        if (currentUserId != userId) {
            return ApiResponse.error("You are not authorized to delete this user")
        }

        val result = deleteUserUseCase.execute(DeleteUserDto.Request(currentUserId))
        return if (result.isSuccess) {
            ApiResponse.success(
                data = Unit,
                message = "用户删除成功喵"
            )
        } else {
            // 失败：返回 400/404 错误
            val exception = result.exceptionOrThrow()
            val status = if (exception.message.contains("不存在")) {
                HttpStatus.NOT_FOUND
            } else {
                HttpStatus.BAD_REQUEST
            }
            ApiResponse.error(result.exceptionOrNull() ?: exception)
        }
    }
}