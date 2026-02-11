package dev.saraki.wofuf.modules.players.useCases.getPlayer

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
@RequestMapping("/api/v1/players/profile")
class GetPlayerController : BaseController() {
    @Autowired
    private lateinit var getPlayerUseCase: GetPlayerUseCase

    @GetMapping("/{playerNameOrUuid}")
    fun getPlayerData(
        @PathVariable playerNameOrUuid: String
    ): ApiResponse<GetPlayerDto.Response> {
        val result = getPlayerUseCase.execute(
            GetPlayerDto.Request(
                playerNameOrUuid,
            )
        ).getOrThrow()
        return ApiResponse.success(
            GetPlayerDtoMap.from(result)
        )
    }
}