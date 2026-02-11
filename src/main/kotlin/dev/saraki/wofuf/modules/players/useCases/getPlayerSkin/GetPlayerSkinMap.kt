package dev.saraki.wofuf.modules.players.useCases.getPlayerSkin

import dev.saraki.wofuf.modules.players.domain.PlayerSkin

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/2 17:56
 *   @description:
 */
object GetPlayerSkinMapper {
    fun from(playerSkin: PlayerSkin): GetPlayerSkinDto.Response {
        return GetPlayerSkinDto.Response(
            type = playerSkin.type,
            skin = playerSkin.skin,
            cape = playerSkin.cape
        )
    }
}