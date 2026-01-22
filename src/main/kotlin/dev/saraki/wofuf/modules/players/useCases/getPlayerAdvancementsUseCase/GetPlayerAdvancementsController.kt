package dev.saraki.wofuf.modules.players.useCases.getPlayerAdvancementsUseCase

import dev.saraki.wofuf.modules.players.mappers.PlayerMap
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.beans.factory.annotation.Autowired
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
@RequestMapping("/api/v1/players/advancements")
class GetPlayerAdvancementsController: BaseController() {
    @Autowired
    private lateinit var getPlayerAdvancementsUseCase: GetPlayerAdvancementsUseCase

    @GetMapping("/{playerName}")
    fun getPlayerAdvancements(
        @PathVariable playerName: String
    ): ApiResponse<GetPlayerAdvancementsView> {
        val result = getPlayerAdvancementsUseCase.execute(
            GetPlayerAdvancementsCommand(
                playerName,
            )
        )
        return ApiResponse.success(
            GetPlayerAdvancementsView(
                PlayerMap.from(result.getOrThrow()).advancements
            )
        )
    }
}