package dev.saraki.wofuf.shared.core

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/14 16:43
 *   @description:
 */
/**
 * Marker/utility for domain objects that can carry a set of changes (key -> value).
 * Implementations can expose a map of changed fields.
 */
interface WithChanges {
    val changes: Changes
}