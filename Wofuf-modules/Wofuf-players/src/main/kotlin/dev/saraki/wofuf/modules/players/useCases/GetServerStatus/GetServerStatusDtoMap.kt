package dev.saraki.wofuf.modules.players.useCases.getServerStatus

import dev.saraki.wofuf.modules.players.domain.ServerStatus

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/4/8
 *   @description: Mapper for ServerStatus domain object to DTO
 */
abstract class GetServerStatusDtoMap {
    companion object {
        fun from(status: ServerStatus): GetServerStatusDto.Response =
            GetServerStatusDto.Response(
                onlinePlayers = status.onlinePlayers,
                maxPlayers = status.maxPlayers,
                tps = status.tps.stringValue,
                heartbeatStatus = status.heartbeatStatus,
                updateTime = status.updateTime
            )
    }
}
