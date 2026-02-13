package dev.saraki.wofuf.modules.users.useCases.refreshAccessToken

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 21:34
 *   @description:
 */
import dev.saraki.wofuf.modules.users.config.UserApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(UserApiConstantV1.Me.TOKENS)
class RefreshAccessTokenController(
    val refreshAccessTokenUseCase: RefreshAccessTokenUseCase
) : BaseController() {
    @PostMapping
    fun refreshAccessToken(@RequestBody request: RefreshAccessTokenDto.Request): ApiResponse<RefreshAccessTokenDto.Response> {
        val result = refreshAccessTokenUseCase.execute(request)
        if (result.isFailure) {
            return ApiResponse.error(result.exceptionOrThrow())
        }
        return ApiResponse.success(result.getOrNull()!!)
    }
}