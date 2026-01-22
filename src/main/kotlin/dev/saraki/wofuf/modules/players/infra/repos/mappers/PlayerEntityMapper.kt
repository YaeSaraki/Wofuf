package dev.saraki.wofuf.modules.players.infra.repos.mappers

import dev.saraki.wofuf.modules.players.domain.*
import dev.saraki.wofuf.modules.players.infra.repos.entities.PlayerEntity

object PlayerEntityMapper {

    fun toDomain(entity: PlayerEntity): Player =
        Player(
            id = PlayerId.create(entity.uuid!!).getOrThrow(),
            props = PlayerProps(
                name = entity.name!!,
                firstLogin = entity.firstLogin!!,
                lastLogin = entity.lastLogin!!,
                totalPlaytimeSeconds = entity.totalPlaytime!!,
                updateTime = entity.updateTime!!
            ),
            statistics = PlayerJsonMapper.statisticsFromJson(entity.statisticsJson),
            advancements = PlayerJsonMapper.advancementsFromJson(entity.advancementsJson)
        )
}
