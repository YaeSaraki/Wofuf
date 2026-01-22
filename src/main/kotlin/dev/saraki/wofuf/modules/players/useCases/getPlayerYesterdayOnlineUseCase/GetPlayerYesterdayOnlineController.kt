package dev.saraki.wofuf.modules.players.useCases.getPlayerYesterdayOnlineUseCase

import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/22 18:39
 *   @description:
 */
@RestController
@RequestMapping("/api/v1/players/yesterday")
class GetPlayerYesterdayOnlineController(
    private val getPlayerYesterdayOnlineUseCase: GetPlayerYesterdayOnlineUseCase,
) {
    @GetMapping()
    fun yesterdayLoginPlayers(): ApiResponse<List<GetPlayerYesterdayOnlineDto>> {
        return ApiResponse.success(
            getPlayerYesterdayOnlineUseCase.execute(Unit).getOrThrow()
        )
    }
}