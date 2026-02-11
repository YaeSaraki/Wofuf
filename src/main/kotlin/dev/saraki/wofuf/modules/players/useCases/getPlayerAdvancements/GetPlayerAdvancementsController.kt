package dev.saraki.wofuf.modules.players.useCases.getPlayerAdvancements

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
@RequestMapping("/api/v1/players/advancements")
class GetPlayerAdvancementsController : BaseController() {
    @Autowired
    private lateinit var getPlayerAdvancementsUseCase: GetPlayerAdvancementsUseCase

    @GetMapping("/{playerUuid}")
    fun getPlayerAdvancements(
        @PathVariable playerUuid: String,
        @RequestParam includeRecipes: Boolean = false
    ): ApiResponse<GetPlayerAdvancementsDto.Response> {
        val result = getPlayerAdvancementsUseCase.execute(
            GetPlayerAdvancementsDto.Request(
                playerUuid,
            )
        )
        return ApiResponse.success(
            GetPlayerAdvancementsDto.Response(
                PlayerMap.from(result.getOrThrow())
                    .advancements
                    .filter { includeRecipes || !it.key.startsWith("recipes") } // 简化条件
            )
        )
    }
}