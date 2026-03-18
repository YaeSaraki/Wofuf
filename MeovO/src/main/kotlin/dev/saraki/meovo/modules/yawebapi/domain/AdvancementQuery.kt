package dev.saraki.meovo.modules.yawebapi.domain

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 11:40
 *   @description:
 */
class AdvancementQuery(
    val includeDone: Boolean = true
) {
    companion object {
        fun from(params: Map<String, List<String>>): AdvancementQuery {
            val includeDone =
                params["done"]?.firstOrNull()?.toBooleanStrictOrNull() ?: true
            return AdvancementQuery(includeDone)
        }
    }
}