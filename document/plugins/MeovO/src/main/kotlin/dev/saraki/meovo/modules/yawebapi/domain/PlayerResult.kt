package dev.saraki.meovo.modules.yawebapi.domain

import java.util.UUID

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 11:51
 *   @description:
 */
data class PlayerResult(
    val uuid: UUID,
    val name: String,
    val online: Boolean,
    val world: String,
    val firstLogin: Long,
    val lastLogin: Long,
    val totalPlaytimeSeconds: Long,
    val x: Double? = null,
    val y: Double? = null,
    val z: Double? = null
)