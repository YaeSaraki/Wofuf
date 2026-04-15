package dev.saraki.wofuf.modules.players.useCases.getPlayerStatistics

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
@RequestMapping(PlayerApiConstantV1.Data.STATISTICS)
class GetPlayerStatisticsController(
    private val getPlayerStatisticsUseCase: GetPlayerStatisticsUseCase
) : BaseController() {

    @GetMapping
    fun getPlayerStatistics(
        @PathVariable playerUuid: String,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) categories: Array<String>?,
        @RequestParam(required = false) key: String?,
        @RequestParam(required = false) keys: Array<String>?
    ): ApiResponse<GetPlayerStatisticsDto.Response> {
        val result = getPlayerStatisticsUseCase.execute(
            GetPlayerStatisticsDto.Request(playerUuid, category, categories, key, keys)
        )
        return if (result.isFailure) {
            ApiResponse.error(result.exceptionOrThrow())
        } else {
            ApiResponse.success(result.getOrThrow())
        }
    }
}