package dev.saraki.wofuf.modules.players.useCases.getPlayerStatisticsUseCase

import dev.saraki.wofuf.modules.players.mappers.PlayerMap
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 15:30
 *   @description:
 */
@RestController
@RequestMapping("/api/v1/players/statistics")
class GetPlayerStatisticsController: BaseController() {
    @Autowired
    private lateinit var getPlayerStatisticsUseCase: GetPlayerStatisticsUseCase

    @GetMapping("/{playerName}")
    fun getPlayerStatistics(
        @PathVariable playerName: String,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) key: String?
    ): ApiResponse<GetPlayerStatisticsView> {
        val result = getPlayerStatisticsUseCase.execute(
            GetPlayerStatisticsCommand(
                playerName,
                category,
                key,
            )
        )
        return ApiResponse.success(
            GetPlayerStatisticsView(
                PlayerMap.from(result.getOrThrow()).statistics
            )
        )
    }
}