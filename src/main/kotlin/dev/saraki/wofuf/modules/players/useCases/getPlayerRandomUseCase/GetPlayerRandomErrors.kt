package dev.saraki.wofuf.modules.players.useCases.getPlayerRandomUseCase

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 11:59
 *   @description:
 */
class GetPlayerRandomErrors {
    // Failed to get random player
    class GetRandomPlayerError() : Result.Failure<List<Player>>(
        exception = UseCaseError(
            code = "GET_RANDOM_PLAYER_ERROR",
            message = "Failed to get random player"
        )
    )
}