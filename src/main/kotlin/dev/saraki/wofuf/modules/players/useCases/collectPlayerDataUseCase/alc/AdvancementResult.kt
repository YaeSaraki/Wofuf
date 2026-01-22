package dev.saraki.wofuf.modules.players.useCases.collectPlayerDataUseCase.alc

import dev.saraki.wofuf.modules.players.domain.PlayerAdvancement

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 00:43
 *   @description:
 */
data class AdvancementResult(
    val uuid: String,
    val name: String,
    val advancements: List<PlayerAdvancement>
)