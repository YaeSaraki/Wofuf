package dev.saraki.meovo.modules.yawebapi.domain

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 11:40
 *   @description:
 */
data class AdvancementResult(
    val uuid: String,
    val name: String,
    val advancements: List<AdvancementItem>
)