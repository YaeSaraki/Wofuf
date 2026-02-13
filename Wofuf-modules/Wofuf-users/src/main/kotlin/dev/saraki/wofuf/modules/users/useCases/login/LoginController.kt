package dev.saraki.wofuf.modules.users.useCases.login

import dev.saraki.wofuf.modules.users.config.UserApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 11:06
 *   @description:
 */
@RestController
@RequestMapping(UserApiConstantV1.Me.SESSIONS)
class LoginController(
    private val loginUseCase: LoginUseCase
): BaseController() {
    @PostMapping
    fun login(@Valid @RequestBody request: LoginDto.Request): ApiResponse<LoginDto.Response> {
        if (request.username.isBlank() || request.password.isBlank()) {
            return ApiResponse.error("Username or password is blank")
        }
        val result = loginUseCase.execute(request)
        if (result.isFailure) {
            return ApiResponse.error(result.exceptionOrThrow())
        }
        return ApiResponse.success(result.getOrNull()!!)
    }
}
