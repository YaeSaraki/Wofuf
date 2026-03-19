package dev.saraki.wofuf.modules.players.useCases.searchPlayers

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

class SearchPlayersErrors {

    class QueryEmptyError : Result.Failure<SearchPlayersDto.Response>(
        exception = UseCaseError(
            code = "QUERY_EMPTY_ERROR",
            message = "Search query cannot be empty"
        )
    )

    class InvalidLimitError : Result.Failure<SearchPlayersDto.Response>(
        exception = UseCaseError(
            code = "INVALID_LIMIT_ERROR",
            message = "Limit must be a positive number"
        )
    )
}
