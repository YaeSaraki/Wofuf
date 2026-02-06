package dev.saraki.wofuf.modules.players.useCases.getPlayerUseCase

import dev.saraki.wofuf.modules.players.domain.Player

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 23:16
 *   @description:
 */
abstract class GetPlayerViewMap {
    companion object {
        fun from(player: Player): GetPlayerView =
            GetPlayerView(
                id = player.playerId.stringValue,
                name = player.playerName,
                firstLogin = player.firstLogin,
                lastLogin = player.lastLogin,
                totalPlaytimeSeconds = player.totalPlaytimeSeconds,
                updateTime = player.updateTime,
            )
    }
}