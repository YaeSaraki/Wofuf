package dev.saraki.wofuf.modules.players.useCases.getPlayerStatisticsUseCase

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 13:49
 *   @description:
 */
class GetPlayerStatisticsErrors {
    // Failed to get player
    class GetPlayerError() : Result.Failure<Player>(
        exception = UseCaseError(
            code = "GET_PLAYER_ERROR",
            message = "Failed to get player, player not found"
        )
    )
}