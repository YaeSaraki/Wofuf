package dev.saraki.wofuf.modules.players.useCases.getPlayerSkin

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/2 17:40
 *   @description:
 */
class GetPlayerSkinDto {
    data class Request(val playerUuid: String)
    data class Response(
        val type: String,
        val skin: String,
        val cape: String,
    )
}