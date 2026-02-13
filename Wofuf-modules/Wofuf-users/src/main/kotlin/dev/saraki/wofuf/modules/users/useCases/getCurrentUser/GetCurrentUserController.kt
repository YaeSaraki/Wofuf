package dev.saraki.wofuf.modules.users.useCases.getCurrentUser

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/17 22:20
 *   @description:
 */

import dev.saraki.wofuf.modules.users.config.UserApiConstantV1
import dev.saraki.wofuf.modules.users.services.auth.UserAuthService
import dev.saraki.wofuf.modules.users.useCases.getUserByUsername.GetUserByUsernameDto
import dev.saraki.wofuf.modules.users.useCases.getUserByUsername.GetUserByUsernameUseCase
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(UserApiConstantV1.Base.ME)
class GetCurrentUserController(
    private val userAuthService: UserAuthService,
    private val getUserByUsernameUseCase: GetUserByUsernameUseCase
) : BaseController() {

    @GetMapping
    fun getUserByUsername(@RequestHeader("MeoKey") token: String): ApiResponse<GetUserByUsernameDto.Response> {
        val jwtClaims = userAuthService.authenticate(token) ?: return ApiResponse.error("Invalid token")

        val username = jwtClaims.username
        val getUserByUsernameDto = GetUserByUsernameDto.Request(username)
        val resultOrError = getUserByUsernameUseCase.execute(getUserByUsernameDto)

        if (resultOrError.isFailure) {
            return ApiResponse.error(resultOrError.exceptionOrThrow())
        }
        val result = resultOrError.getOrThrow()
        return ApiResponse.success(result)
    }
}