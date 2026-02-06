package dev.saraki.wofuf.modules.players.infra.repos.mappers

import dev.saraki.wofuf.modules.players.domain.*
import dev.saraki.wofuf.modules.players.infra.repos.entities.PlayerEntity
import dev.saraki.wofuf.shared.core.Guard
import dev.saraki.wofuf.shared.domain.UniqueEntityId

object PlayerEntityMapper {

    fun toDomain(entity: PlayerEntity): Player {
        val guardResult = Guard.againstNullOrUndefinedBulk(
            listOf(
                Guard.GuardArgument(entity.uuid, "name"),
            )
        )

        if (guardResult.isFailure) {
            throw guardResult.exceptionOrThrow()
        }

        val playerOrError = Player.create(
            props = PlayerProps(
                name = entity.name!!,
                firstLogin = entity.firstLogin!!,
                lastLogin = entity.lastLogin!!,
                totalPlaytimeSeconds = entity.totalPlaytime!!,
                updateTime = entity.updateTime!!,
                statistics = PlayerJsonMapper.statisticsFromJson(entity.statisticsJson),
                advancements = PlayerJsonMapper.advancementsFromJson(entity.advancementsJson),
                playerSkin = PlayerSkinMapper.toDomain(entity.playerSkin)
            ),
            id = UniqueEntityId(entity.uuid!!)
        )

        return playerOrError.getOrThrow()
    }
}
