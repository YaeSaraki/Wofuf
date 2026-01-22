package dev.saraki.wofuf.modules.users.useCases.refreshAccessToken

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/18 21:34
 *   @description:
 */

import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users/token/refresh")
class RefreshAccessTokenController(@Autowired val refreshAccessTokenUseCase: RefreshAccessTokenUseCase) : BaseController() {

    @PostMapping("")
    fun refreshAccessToken(@RequestBody request: RefreshAccessTokenDto.RefreshAccessTokenRequest): ApiResponse<RefreshAccessTokenDto.RefreshAccessTokenResponse> {
        val result = refreshAccessTokenUseCase.execute(request)
        if (result.isFailure) {
            return ApiResponse.error(result.exceptionOrThrow())
        }
        return ApiResponse.success(result.getOrNull()!!)
    }
}