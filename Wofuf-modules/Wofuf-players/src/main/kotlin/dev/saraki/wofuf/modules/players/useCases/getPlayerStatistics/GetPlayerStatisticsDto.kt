package dev.saraki.wofuf.modules.players.useCases.getPlayerStatistics

import dev.saraki.wofuf.modules.players.dtos.PlayerStatisticDto

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 13:45
 *   @description:
 */
class GetPlayerStatisticsDto {
    data class Request(
        val playerUuid: String,
        val category: String? = null,
        val categories: Array<String>? = null,
        val key: String? = null,
        val keys: Array<String>? = null
    )
    data class Response(val statistics: Map<String, PlayerStatisticDto>)
}