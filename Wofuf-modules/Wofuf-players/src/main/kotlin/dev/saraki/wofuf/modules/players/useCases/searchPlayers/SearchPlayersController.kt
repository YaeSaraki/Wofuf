package dev.saraki.wofuf.modules.players.useCases.searchPlayers

import dev.saraki.wofuf.modules.players.config.PlayerApiConstantV1
import dev.saraki.wofuf.shared.infra.http.api.v1.models.ApiResponse
import dev.saraki.wofuf.shared.infra.http.api.v1.models.BaseController
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(PlayerApiConstantV1.Features.SEARCH)
class SearchPlayersController(
    private val searchPlayersUseCase: SearchPlayersUseCase
) : BaseController() {

    @GetMapping
    fun searchPlayers(
        @RequestParam query: String,
        @RequestParam(defaultValue = "20") limit: Int
    ): ApiResponse<SearchPlayersDto.Response> {
        val result = searchPlayersUseCase.execute(
            SearchPlayersDto.Request(query, limit)
        ).getOrThrow()
        return ApiResponse.success(result)
    }
}
