package dev.saraki.wofuf.modules.users.useCases.logout

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 21:34
 *   @description:
 */

import dev.saraki.wofuf.modules.users.config.UserApiConstantV1
import dev.saraki.wofuf.modules.users.services.auth.UserAuthService
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(UserApiConstantV1.Me.SESSIONS)
class LogoutController(
    private val logoutUseCase: LogoutUseCase,
    private val userAuthService: UserAuthService
) : BaseController() {

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(@RequestHeader("MeoKey") token: String): ApiResponse<LogoutDto.Response> {
        val jwtClaims = userAuthService.authenticate(token) ?: return ApiResponse.error("Invalid token")
        val userId = jwtClaims.userId
        val result = logoutUseCase.execute(LogoutDto.Request(userId, token))

        if (result.isFailure) {
            return ApiResponse.error(result.exceptionOrThrow())
        }

        return ApiResponse.success(result.getOrNull()!!)
    }
}