package dev.saraki.wofuf.modules.users.useCases.getCurrentUser

import dev.saraki.wofuf.modules.users.config.UserApiConstantV1
import dev.saraki.wofuf.modules.users.infra.security.requireCurrentUserId
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(UserApiConstantV1.Base.ME)
class GetCurrentUserController(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : BaseController() {

    @GetMapping
    fun getCurrentUser(): ApiResponse<GetCurrentUserDto.Response> {
        val currentUserId = requireCurrentUserId()
        
        val result = getCurrentUserUseCase.execute(GetCurrentUserDto.Request(currentUserId))
        
        if (result.isFailure) {
            return ApiResponse.error(result.exceptionOrThrow())
        }
        
        return ApiResponse.success(result.getOrThrow())
    }
}
