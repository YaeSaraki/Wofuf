package dev.saraki.wofuf.modules.players.useCases.getPlayerAdvancements

import dev.saraki.wofuf.modules.players.dtos.PlayerAdvancementDto

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 13:45
 *   @description:
 */
class GetPlayerAdvancementsDto{
    data class Request(val playerUuid: String)
    data class Response(val advancements: List<PlayerAdvancementDto>)
}