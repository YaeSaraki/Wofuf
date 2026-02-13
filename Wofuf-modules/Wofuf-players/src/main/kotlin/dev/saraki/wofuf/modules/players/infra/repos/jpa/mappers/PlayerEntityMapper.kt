package dev.saraki.wofuf.modules.players.infra.repos.jpa.mappers

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerName
import dev.saraki.wofuf.modules.players.domain.PlayerProps
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerAdvancement
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerStatistic
import dev.saraki.wofuf.modules.players.infra.repos.jpa.entities.PlayerEntity
import dev.saraki.wofuf.modules.players.infra.repos.jpa.mappers.player.PlayerJsonMapper
import dev.saraki.wofuf.modules.players.infra.repos.jpa.mappers.player.PlayerSkinMapper
import dev.saraki.wofuf.shared.domain.UniqueEntityId

object PlayerEntityMapper {

    fun toDomain(playerEntity: PlayerEntity): Player {

        val player = Player.create(
            props = PlayerProps(
                playerName = PlayerName.create(playerEntity.playerName).getOrThrow(),
                firstLogin = playerEntity.firstLogin,
                lastLogin = playerEntity.lastLogin,
                totalPlaytimeSeconds = playerEntity.totalPlaytime,
                updateTime = playerEntity.updateTime,
                statistics = playerEntity.statisticsJson.toStatisticsMap(),
                advancements = playerEntity.advancementsJson.toAdvancementsMap(),
                playerSkin = PlayerSkinMapper.toDomain(playerEntity.playerSkin)
            ),
            id = UniqueEntityId(playerEntity.playerId),
        ).getOrThrow()

        player._createdAt = playerEntity.createdAt
        player._updatedAt = playerEntity.updatedAt

        return player
    }

    fun toEntity(player: Player): PlayerEntity {
        val entity = PlayerEntity(
            playerId = player.playerId.stringValue,
            playerName = player.playerName.stringValue,
            firstLogin = player.firstLogin,
            lastLogin = player.lastLogin,
            totalPlaytime = player.totalPlaytimeSeconds,
            updateTime = player.updateTime,
            statisticsJson = player.statistics.toStatisticsJson(),
            advancementsJson = player.advancements.toAdvancementsJson(),
            playerSkin = PlayerSkinMapper.toEntity(player.playerSkin),
        )
        return entity
    }

    // 为不同类型的 Map 使用不同的函数名
    fun Map<String, PlayerStatistic>.toStatisticsJson(): String =
        PlayerJsonMapper.statisticsToJson(this.mapValues { it.value.props })

    fun Map<String, PlayerAdvancement>.toAdvancementsJson(): String =
        PlayerJsonMapper.advancementsToJson(this.mapValues { it.value.props })

    fun String?.toStatisticsMap(): Map<String, PlayerStatistic> =
        PlayerJsonMapper.statisticsFromJson(this).mapValues { (_, props) ->
            PlayerStatistic.create(props).getOrElse { PlayerStatistic.defaultProps }
        }

    fun String?.toAdvancementsMap(): Map<String, PlayerAdvancement> =
        PlayerJsonMapper.advancementsFromJson(this).mapValues { (_, props) ->
            PlayerAdvancement.create(props).getOrElse { PlayerAdvancement.defaultProps }
        }
}
