package dev.saraki.wofuf.modules.players.useCases.getPlayerRandom

import dev.saraki.wofuf.modules.players.config.PlayerApiConstantV1
import dev.saraki.wofuf.modules.players.useCases.getPlayer.GetPlayerDto
import dev.saraki.wofuf.modules.players.useCases.getPlayer.GetPlayerDtoMap
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
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
@RequestMapping(PlayerApiConstantV1.Features.RANDOM_PROFILE)
class GetPlayerRandomController(
    private val getRandomPlayerUseCase: GetPlayerRandomUseCase
) : BaseController() {

    @GetMapping
    fun getRandomPlayer(@RequestParam count: Int = 1): ApiResponse<List<GetPlayerDto.Response>> {
        val result = getRandomPlayerUseCase.execute(count)
        if (result.isFailure) {
            return ApiResponse.error(result.exceptionOrThrow())
        }
        return ApiResponse.success(result.getOrThrow().map(GetPlayerDtoMap::from))
    }
}