package dev.saraki.wofuf.modules.players.mappers

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.dtos.PlayerAdvancementDto
import dev.saraki.wofuf.modules.players.dtos.PlayerDto
import dev.saraki.wofuf.modules.players.dtos.PlayerSkinDto
import dev.saraki.wofuf.modules.players.dtos.PlayerStatisticDto

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/20 13:30
 *   @description:
 */
abstract class PlayerMap {
    companion object {
        fun from(player: Player): PlayerDto =
            PlayerDto(
                uuid = player.playerId.stringValue,
                name = player.playerName,
                firstLogin = player.firstLogin,
                lastLogin = player.lastLogin,
                totalPlaytimeSeconds = player.totalPlaytimeSeconds,
                updateTime = player.updateTime,
                statistics = player.statistics.mapValues {
                    PlayerStatisticDto(
                        category = it.value.category,
                        key = it.value.key,
                        value = it.value.value,
                    )

                },
                advancements = player.advancements.values.map {
                    PlayerAdvancementDto(
                        key = it.key,
                        done = it.done,
                        completed = it.completed,
                        remaining = it.remaining
                    )
                },
                playerSkin = player.playerSkin.let {
                    PlayerSkinDto(
                        type = it.type,
                        skin = it.skin,
                        cape = it.cape,
                    )
                }
            )
    }
}