package dev.saraki.wofuf.modules.players.useCases.getServerStatus

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/4/8
 *   @description: DTOs for GetServerStatus use case
 */
class GetServerStatusDto {
    data class Request(
        val forceRefresh: Boolean = false
    )

    data class Response(
        val onlinePlayers: Int,
        val maxPlayers: Int,
        val tps: String,
        val heartbeatStatus: Boolean,
        val updateTime: Long
    )
}
