package dev.saraki.wofuf.modules.players.useCases.getPlayerSkin

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
 *   @date 2026/2/2 17:31
 *   @description:
 */
@RestController
@RequestMapping(PlayerApiConstantV1.Data.SKINS)
class GetPlayerSkinController(
    private val getPlayerSkinUseCase: GetPlayerSkinUseCase
) : BaseController() {

    @GetMapping
    fun getPlayerSkin(@PathVariable playerUuid: String): ApiResponse<GetPlayerSkinDto.Response> {
        val result = getPlayerSkinUseCase.execute(GetPlayerSkinDto.Request(playerUuid))
        if (result.isFailure) {
            return ApiResponse.error(result.exceptionOrThrow())
        }
        return ApiResponse.success(result.getOrThrow())
    }
}