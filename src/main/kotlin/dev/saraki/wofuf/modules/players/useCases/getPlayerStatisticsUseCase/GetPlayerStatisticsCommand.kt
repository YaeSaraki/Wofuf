package dev.saraki.wofuf.modules.players.useCases.getPlayerStatisticsUseCase

import io.lettuce.core.dynamic.annotation.Key

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 13:45
 *   @description:
 */
data class GetPlayerStatisticsCommand (
    val playerUuid: String,
)