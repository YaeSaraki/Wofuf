package dev.saraki.wofuf.modules.players.useCases.getPlayerAdvancements

import dev.saraki.wofuf.modules.players.config.PlayerApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.*

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 15:30
 *   @description:
 */
@RestController
@RequestMapping(PlayerApiConstantV1.Data.ADVANCEMENTS)
class GetPlayerAdvancementsController(
    private val getPlayerAdvancementsUseCase: GetPlayerAdvancementsUseCase
) : BaseController() {

    @GetMapping
    fun getPlayerAdvancements(
        @PathVariable playerUuid: String,
        @RequestParam(defaultValue = "false") includeRecipes: Boolean
    ): ApiResponse<GetPlayerAdvancementsDto.Response> {
        val result = getPlayerAdvancementsUseCase.execute(
            GetPlayerAdvancementsDto.Request(playerUuid, includeRecipes)
        )
        return if (result.isFailure) {
            ApiResponse.error(result.exceptionOrThrow())
        } else {
            ApiResponse.success(result.getOrThrow())
        }
    }
}