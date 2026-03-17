package dev.saraki.meovo.modules.yawebapi.domain

import java.util.UUID

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/2 11:48
 *   @description:
 */
data class SkinResult(
    val name: String,
    val uuid: UUID,
    val type: String,
    val skin: String,
    val cape: String
)