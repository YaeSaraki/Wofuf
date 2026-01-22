package dev.saraki.wofuf.modules.players.useCases.getPlayerYesterdayOnlineUseCase

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 13:49
 *   @description:
 */
class GetPlayerYesterdayOnlineErrors {
    // Failed to get player
    class GetPlayerError() : Result.Failure<List<GetPlayerYesterdayOnlineDto>>(
        exception = UseCaseError(
            code = "GET_PLAYER_ERROR",
            message = "Failed to get player, player not found"
        )
    )
}