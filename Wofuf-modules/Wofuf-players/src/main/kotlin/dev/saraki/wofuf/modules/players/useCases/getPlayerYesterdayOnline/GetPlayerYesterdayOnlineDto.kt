package dev.saraki.wofuf.modules.players.useCases.getPlayerYesterdayOnline

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/22 18:28
 *   @description:
 */
class GetPlayerYesterdayOnlineDto {
    data class Response(
        val playerNames: List<String>
    )
}