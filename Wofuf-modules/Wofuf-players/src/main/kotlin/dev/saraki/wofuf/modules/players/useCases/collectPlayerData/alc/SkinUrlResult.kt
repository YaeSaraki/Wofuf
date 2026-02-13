package dev.saraki.wofuf.modules.players.useCases.collectPlayerData.alc

import java.util.*

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/2 16:13
 *   @description:
 */
data class SkinUrlResult(
    val name: String,
    val uuid: UUID,
    val type: String,
    val skin: String,
    val cape: String
)