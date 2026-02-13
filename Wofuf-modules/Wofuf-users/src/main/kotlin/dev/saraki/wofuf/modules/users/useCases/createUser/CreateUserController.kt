package dev.saraki.wofuf.modules.users.useCases.createUser

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/16 17:27
 *   @description:
 */

import dev.saraki.wofuf.modules.users.config.UserApiConstantV1
import dev.saraki.wofuf.modules.users.mappers.UserMap
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(UserApiConstantV1.Base.ROOT)
class CreateUserController(
    private val createUserUseCase: CreateUserUseCase
) : BaseController() {
    /**
     * 创建用户
     * @param request 创建用户请求参数（已做 JSR-380 校验）
     * @return 标准化响应，包含用户信息
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createUser(@Valid @RequestBody request: CreateUserDto.Request): ApiResponse<CreateUserDto.Response> {
        val result = createUserUseCase.execute(request)

        return if (result.isSuccess) {
            // 成功：返回用户信息
            val user = result.getOrThrow()
            ApiResponse.success(
                data = CreateUserDto.Response(
                    userId = user.userId.stringValue,
                    username = user.username.value,
                    email = user.email.value
                ),
                message = "用户创建成功"
            )
        } else {
            ApiResponse.error(result.exceptionOrThrow())
        }
    }
}