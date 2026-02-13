package dev.saraki.wofuf.modules.players.useCases.getPlayerStatistics

import dev.saraki.wofuf.modules.players.dtos.PlayerStatisticDto

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 13:45
 *   @description:
 */
class GetPlayerStatisticsDto {
    data class Request(val playerUuid: String)
    data class Response(val statistics: Map<String, PlayerStatisticDto>)
}