package dev.saraki.wofuf.modules.users.useCases.login

import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
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
@RequestMapping("/api/v1/users/login")
class LoginController: BaseController() {
    @Autowired
    private lateinit var loginUseCase: LoginUseCase
    @PostMapping("")
    fun login(@RequestBody request: LoginDto.LoginRequest): ApiResponse<LoginDto.LoginResponse> {
        val result = loginUseCase.execute(request)
        if (result.isFailure) {
            return ApiResponse.error(result.exceptionOrThrow())
        }
        return ApiResponse.success(result.getOrNull()!!)
    }
}
