package dev.saraki.wofuf.modules.players.useCases.getPlayer

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 13:45
 *   @description: GetPlayer use case的DTO类，封装Request和Response
 */
class GetPlayerDto {
    data class Request(
        val playerNameOrUuid: String,
    )


    data class Response(
        val id: String,
        val name: String,
        val firstLogin: Long,
        val lastLogin: Long,
        val totalPlaytimeSeconds: Long,
        val updateTime: Long
    )
}