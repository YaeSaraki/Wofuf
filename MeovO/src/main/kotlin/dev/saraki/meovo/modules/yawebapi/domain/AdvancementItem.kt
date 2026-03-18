package dev.saraki.meovo.modules.yawebapi.domain

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 11:41
 *   @description:
 */
data class AdvancementItem (
    val key: String,
    val done: Boolean,
    val completed: List<String>,
    val remaining: List<String>
)