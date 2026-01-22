package dev.saraki.wofuf.modules.players.useCases.getPlayerStatisticsUseCase

import dev.saraki.wofuf.modules.players.dtos.PlayerStatisticDto

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 22:19
 *   @description:
 */
class GetPlayerStatisticsView (
    val statistics: Map<String, PlayerStatisticDto>
) {
}