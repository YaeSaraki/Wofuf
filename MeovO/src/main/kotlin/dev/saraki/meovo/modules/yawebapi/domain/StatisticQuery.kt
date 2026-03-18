package dev.saraki.meovo.modules.yawebapi.domain

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 11:19
 *   @description:
 */
class StatisticQuery(
    val category: String?,
    val key: String?,
    val includeOffline: Boolean
) {
    companion object {
        fun from(params: Map<String, List<String>>) = StatisticQuery(
            category = params["category"]?.firstOrNull(),
            key = params["key"]?.firstOrNull(),
            includeOffline = params["offline"]?.firstOrNull()?.toBoolean() ?: true
        )
    }
}