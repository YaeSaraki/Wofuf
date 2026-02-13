package dev.saraki.wofuf.modules.players.useCases.getPlayerAdvancements

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerId
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
class GetPlayerAdvancementsUseCase(private val playerRepository: PlayerRepo) :
    UseCase<GetPlayerAdvancementsDto.Request, Player> {
    override fun execute(request: GetPlayerAdvancementsDto.Request): Result<Player> {

        val playerIdOrError = PlayerId.create(UniqueEntityId(request.playerUuid))
        if (playerIdOrError.isFailure) {
            return Result.failure(playerIdOrError.exceptionOrThrow())
        }
        val playerId = playerIdOrError.getOrThrow()

        val player = playerRepository.findByPlayerId(playerId)
            ?: return GetPlayerAdvancementsErrors.GetPlayerError()
        return Result.success(player)
    }
}