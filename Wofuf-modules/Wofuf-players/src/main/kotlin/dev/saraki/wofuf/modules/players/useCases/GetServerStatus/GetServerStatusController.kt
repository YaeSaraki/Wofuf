package dev.saraki.wofuf.modules.players.useCases.getServerStatus

import dev.saraki.wofuf.modules.players.config.PlayerApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/4/8
 *   @description: REST controller for server status endpoint
 */
@RestController
@RequestMapping(PlayerApiConstantV1.Features.SERVER_STATUS)
class GetServerStatusController(
    private val getServerStatusUseCase: GetServerStatusUseCase
) : BaseController() {

    @GetMapping
    fun getServerStatus(
        @RequestParam(defaultValue = "false") forceRefresh: Boolean
    ): ApiResponse<GetServerStatusDto.Response> {
        val result = getServerStatusUseCase.execute(
            GetServerStatusDto.Request(forceRefresh)
        ).getOrThrow()

        return ApiResponse.success(GetServerStatusDtoMap.from(result))
    }
}
