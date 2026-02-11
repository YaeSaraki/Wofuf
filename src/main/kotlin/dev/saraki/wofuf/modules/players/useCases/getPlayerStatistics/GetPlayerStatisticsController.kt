package dev.saraki.wofuf.modules.players.useCases.getPlayerStatistics

import dev.saraki.wofuf.modules.players.mappers.PlayerMap
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 15:30
 *   @description:
 */
@RestController
@RequestMapping("/api/v1/players/statistics")
class GetPlayerStatisticsController : BaseController() {
    @Autowired
    private lateinit var getPlayerStatisticsUseCase: GetPlayerStatisticsUseCase

    @GetMapping("/{playerUuid}")
    fun getPlayerStatistics(
        @PathVariable playerUuid: String,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) categories: Array<String?>?,
        @RequestParam(required = false) key: String?,
        @RequestParam(required = false) keys: Array<String?>?
    ): ApiResponse<GetPlayerStatisticsDto.Response> {
        val result = getPlayerStatisticsUseCase.execute(
            GetPlayerStatisticsDto.Request(
                playerUuid,
            )
        )
        return ApiResponse.success(
            GetPlayerStatisticsDto.Response(
                PlayerMap.from(result.getOrThrow()).statistics.filter {
                    (categories?.contains(it.value.category) ?: true)
                            && (keys?.contains(it.value.key) ?: true)
                            && (category == null || it.value.category == category)
                            && (key == null || it.value.key == key)
                }
            )
        )
    }
}