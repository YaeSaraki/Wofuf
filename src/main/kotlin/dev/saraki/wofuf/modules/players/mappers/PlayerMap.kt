package dev.saraki.wofuf.modules.players.mappers

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.dtos.PlayerAdvancementDto
import dev.saraki.wofuf.modules.players.dtos.PlayerDto
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
                uuid = player.id.value.id,
                name = player.props.name,
                firstLogin = player.props.firstLogin,
                lastLogin = player.props.lastLogin,
                totalPlaytimeSeconds = player.props.totalPlaytimeSeconds,
                updateTime = player.props.updateTime,
                statistics = player.statistics.mapValues {
                    PlayerStatisticDto(
                        category = it.key,
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
                }
            )
    }
}