package dev.saraki.wofuf.modules.players.useCases.getPlayerStatistics

import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerId
import dev.saraki.wofuf.modules.players.dtos.PlayerStatisticDto
import dev.saraki.wofuf.modules.players.infra.repos.PlayerRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 13:44
 *   @description:
 */
@Service
class GetPlayerStatisticsUseCase(private val playerRepository: PlayerRepo) :
    UseCase<GetPlayerStatisticsDto.Request, GetPlayerStatisticsDto.Response> {
    override fun execute(request: GetPlayerStatisticsDto.Request): Result<GetPlayerStatisticsDto.Response> {
        val playerIdOrError = PlayerId.create(UniqueEntityId(request.playerUuid))
        if (playerIdOrError.isFailure) {
            return Result.failure(playerIdOrError.exceptionOrThrow())
        }
        val playerId = playerIdOrError.getOrThrow()

        val player = playerRepository.findByPlayerId(playerId)
            ?: return GetPlayerStatisticsErrors.GetPlayerError()

        val filteredStatistics = player.statistics
            .filter { (request.categories?.contains(it.value.category) ?: true)
                    && (request.keys?.contains(it.value.key) ?: true)
                    && (request.category == null || it.value.category == request.category)
                    && (request.key == null || it.value.key == request.key)
            }
            .mapValues { (_, stat) ->
                PlayerStatisticDto(
                    category = stat.category,
                    key = stat.key,
                    value = stat.value
                )
            }

        return Result.success(GetPlayerStatisticsDto.Response(filteredStatistics))
    }
}