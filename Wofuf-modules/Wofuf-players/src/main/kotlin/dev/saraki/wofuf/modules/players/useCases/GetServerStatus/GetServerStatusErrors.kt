package dev.saraki.wofuf.modules.players.useCases.getServerStatus

import dev.saraki.wofuf.modules.players.domain.ServerStatus
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCaseError

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/4/8
 *   @description: Error classes for GetServerStatus use case
 */
class GetServerStatusErrors {
    class ServerStatusNotAvailableError : Result.Failure<ServerStatus>(
        exception = UseCaseError(
            code = "SERVER_STATUS_NOT_AVAILABLE",
            message = "Failed to get server status"
        )
    )
}
