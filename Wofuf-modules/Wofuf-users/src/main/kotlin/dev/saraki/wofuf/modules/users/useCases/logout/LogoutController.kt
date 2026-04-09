package dev.saraki.wofuf.modules.users.useCases.logout

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 21:34
 *   @description:
 */

import dev.saraki.wofuf.modules.users.config.UserApiConstantV1
import dev.saraki.wofuf.modules.users.domain.valueObjects.JwtToken
import dev.saraki.wofuf.modules.users.infra.security.requireCurrentUserId
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
    private val logoutUseCase: LogoutUseCase
) : BaseController() {

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(@RequestHeader("MeoKey") token: JwtToken): ApiResponse<LogoutDto.Response> {
        val currentUserId = requireCurrentUserId()

        val result = logoutUseCase.execute(LogoutDto.Request(currentUserId, token))

        if (result.isFailure) {
            return ApiResponse.error(result.exceptionOrThrow())
        }

        return ApiResponse.success(result.getOrNull()!!)
    }
}