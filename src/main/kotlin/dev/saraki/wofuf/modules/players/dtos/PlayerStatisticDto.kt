package dev.saraki.wofuf.modules.players.dtos

/**
*   @author YaeSaraki 
*   @email ikaraswork@iCloud.com
*   @date 2026/1/20 20:15
*   @description: 
*/
data class PlayerStatisticDto(
    val category: String,
    val key: String,
    val value: Long
)