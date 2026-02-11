package dev.saraki.wofuf.modules.players.useCases.getPlayerSkinUseCase

import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/2 17:39
 *   @description:
 */
class GetPlayerSkinErrors {
    // Failed to get player skin
    class GetPlayerSkinError() : Result.Failure<GetPlayerSkinView>(
        exception = UseCaseError(
            code = "GET_PLAYER_SKIN_ERROR",
            message = "Failed to get player skin, player skin not found"
        )
    )
}