package dev.saraki.wofuf.modules.players.useCases.getPlayerAdvancementsUseCase

import dev.saraki.wofuf.modules.players.dtos.PlayerAdvancementDto

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 22:17
 *   @description:
 */
data class GetPlayerAdvancementsView (
    val advancements: List<PlayerAdvancementDto>
)