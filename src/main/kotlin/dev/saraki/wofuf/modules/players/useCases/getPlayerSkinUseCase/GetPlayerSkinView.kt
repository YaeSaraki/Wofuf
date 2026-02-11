package dev.saraki.wofuf.modules.players.useCases.getPlayerSkinUseCase

import dev.saraki.wofuf.modules.players.domain.PlayerSkin


/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/2 17:39
 *   @description:
 */
data class GetPlayerSkinView(
    val type: String,
    val skin: String,
    val cape: String,
) {
    companion object {
        fun from(playerSkin: PlayerSkin): GetPlayerSkinView {
            return GetPlayerSkinView(
                type = playerSkin.type,
                skin = playerSkin.skin,
                cape = playerSkin.cape
            )
        }
    }
}