package dev.saraki.meovo.modules.yawebapi.domain

import java.util.UUID

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 11:19
 *   @description:
 */
data class StatisticResult(
    val uuid: UUID,
    val name: String?,
    val statistics: Map<String, StatisticItem>
)