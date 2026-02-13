package dev.saraki.wofuf.modules.players.useCases.getPlayer

import dev.saraki.wofuf.modules.players.config.PlayerApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 15:30
 *   @description:
 */
@RestController
@RequestMapping(PlayerApiConstantV1.Base.BY_NAME_OR_UUID)
class GetPlayerController(
    private val getPlayerUseCase: GetPlayerUseCase
) : BaseController() {

    @GetMapping
    fun getPlayerData(
        @PathVariable playerNameOrUuid: String
    ): ApiResponse<GetPlayerDto.Response> {
        val result = getPlayerUseCase.execute(
            GetPlayerDto.Request(
                playerNameOrUuid,
            )
        ).getOrThrow()
        return ApiResponse.success(
            GetPlayerDtoMap.from(result)
        )
    }
}