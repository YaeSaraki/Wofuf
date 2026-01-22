package dev.saraki.wofuf.modules.players.useCases.getPlayerStatisticsUseCase

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 13:45
 *   @description:
 */
data class GetPlayerStatisticsCommand (
    val playerName: String,
    val category: String? = null,
    val key: String? = null,
)