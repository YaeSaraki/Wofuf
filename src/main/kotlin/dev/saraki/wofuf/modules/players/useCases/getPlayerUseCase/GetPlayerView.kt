package dev.saraki.wofuf.modules.players.useCases.getPlayerUseCase

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 22:22
 *   @description:
 */
data class GetPlayerView (
    val id: String,
    val name: String,
    val firstLogin: Long,
    val lastLogin: Long,
    val totalPlaytimeSeconds: Long,
    val updateTime: Long
)