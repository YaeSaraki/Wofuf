package dev.saraki.wofuf.modules.players.infra.repos.jpa.mappers

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.domain.PlayerName
import dev.saraki.wofuf.modules.players.domain.PlayerProps
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
                statistics = PlayerJsonMapper.statisticsFromJson(playerEntity.statisticsJson),
                advancements = PlayerJsonMapper.advancementsFromJson(playerEntity.advancementsJson),
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
            statisticsJson = PlayerJsonMapper.statisticsToJson(player.statistics),
            advancementsJson = PlayerJsonMapper.advancementsToJson(player.advancements),
            playerSkin = PlayerSkinMapper.toEntity(player.playerSkin),
        )
        return entity
    }
}
