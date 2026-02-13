package dev.saraki.wofuf.modules.players.useCases.getPlayer

import dev.saraki.wofuf.modules.players.domain.Player

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 23:16
 *   @description:
 */
abstract class GetPlayerDtoMap {
    companion object {
        fun from(player: Player): GetPlayerDto.Response =
            GetPlayerDto.Response(
                id = player.playerId.stringValue,
                name = player.playerName.stringValue,
                firstLogin = player.firstLogin,
                lastLogin = player.lastLogin,
                totalPlaytimeSeconds = player.totalPlaytimeSeconds,
                updateTime = player.updateTime,
            )
    }
}