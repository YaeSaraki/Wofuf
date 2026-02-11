package dev.saraki.wofuf.modules.players.useCases.getPlayerUseCase

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.domain.PlayerId
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
class GetPlayerUseCase(private val playerRepository: PlayerRepo) : UseCase<GetPlayerCommand, Player> {
    override fun execute(request: GetPlayerCommand): Result<Player> {
        if (request.playerNameOrUuid.isBlank()) {
            return GetPlayerErrors.UserNameOrUuidEmptyError()
        }

        if (request.playerNameOrUuid.length >= 36) {
            val playerUuid = request.playerNameOrUuid
            val playerIdOrError = PlayerId.create(UniqueEntityId(playerUuid))
            if (playerIdOrError.isFailure) {
                return GetPlayerErrors.GetPlayerError()
            }

            val playerId = playerIdOrError.getOrThrow()
            val player = playerRepository.findByPlayerId(playerId)
                ?: return GetPlayerErrors.GetPlayerError()
            return Result.success(player)
        } else {
            val playerName = request.playerNameOrUuid
            val player = playerRepository.findByName(playerName)
                ?: return GetPlayerErrors.GetPlayerError()
            return Result.success(player)
        }
    }
}