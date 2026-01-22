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
                id = player.id.value.id,
                name = player.props.name,
                firstLogin = player.props.firstLogin,
                lastLogin = player.props.lastLogin,
                totalPlaytimeSeconds = player.props.totalPlaytimeSeconds,
                updateTime = player.props.updateTime,
            )
    }
}