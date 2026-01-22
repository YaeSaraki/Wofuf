package dev.saraki.wofuf.modules.players.useCases.getPlayerRandomUseCase

import dev.saraki.wofuf.modules.players.useCases.getPlayerUseCase.GetPlayerView
import dev.saraki.wofuf.modules.players.useCases.getPlayerUseCase.GetPlayerViewMap
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 12:08
 *   @description:
 */
@RestController
@RequestMapping("/api/v1/players/random-profile")
class GetPlayerRandomController: BaseController() {
    @Autowired
    private lateinit var getRandomPlayerUseCase: GetPlayerRandomUseCase

    @GetMapping("")
    fun getRandomPlayer(@RequestParam count: Int = 1): ApiResponse<List<GetPlayerView>> {
        val result = getRandomPlayerUseCase.execute(count)
        if (result.isFailure) {
            return ApiResponse.error(result.exceptionOrThrow())
        }
        return ApiResponse.success(result.getOrThrow().map(GetPlayerViewMap::from))
    }
}