package dev.saraki.wofuf.modules.players.useCases.searchPlayers

import dev.saraki.wofuf.modules.players.infra.repos.PlayerRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service

@Service
class SearchPlayersUseCase(
    private val playerRepo: PlayerRepo
) : UseCase<SearchPlayersDto.Request, SearchPlayersDto.Response> {

    override fun execute(request: SearchPlayersDto.Request): Result<SearchPlayersDto.Response> {
        // Validation: query cannot be blank
        if (request.query.isBlank()) {
            return SearchPlayersErrors.QueryEmptyError()
        }

        // Validation: limit must be positive
        if (request.limit <= 0) {
            return SearchPlayersErrors.InvalidLimitError()
        }

        // Cap the limit to prevent abuse
        val effectiveLimit = minOf(request.limit, MAX_RESULTS)

        // Execute search
        val players = playerRepo.searchByQuery(request.query.trim(), effectiveLimit)

        // Map to DTO
        return Result.success(
            SearchPlayersDto.Response(
                players = players.map { SearchPlayersDtoMap.from(it) }
            )
        )
    }

    companion object {
        private const val MAX_RESULTS = 50
    }
}
