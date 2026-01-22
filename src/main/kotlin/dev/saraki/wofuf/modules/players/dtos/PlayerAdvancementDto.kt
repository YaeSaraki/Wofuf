package dev.saraki.wofuf.modules.players.dtos

/**
*   @author YaeSaraki 
*   @email ikaraswork@iCloud.com
*   @date 2026/1/20 20:16
*   @description: 
*/
data class PlayerAdvancementDto(
    val key: String,
    val done: Boolean,
    val completed: List<String>,
    val remaining: List<String>
)