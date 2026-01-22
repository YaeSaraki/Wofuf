package dev.saraki.wofuf.modules.players.useCases.collectPlayerDataUseCase

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/20 18:09
 *   @description:
 */
class CollectPlayerErrors {
    // 收集玩家信息错误
    class CollectPlayerProfileError(uuid: String) : Result.Failure<Player>(
        exception = UseCaseError(
            code = "COLLECT_PLAYER_PROFILE_ERROR",
            message = "Failed to collect player profile for uuid $uuid"
        )
    )

    // 创建玩家错误
    class CreatePlayerError(uuid: String) : Result.Failure<Player>(
        exception = UseCaseError(
            code = "CREATE_PLAYER_ERROR",
            message = "Failed to create player for uuid $uuid"
        )
    )


}