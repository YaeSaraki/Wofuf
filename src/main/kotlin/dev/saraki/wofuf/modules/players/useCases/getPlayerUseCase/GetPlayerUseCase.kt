package dev.saraki.wofuf.modules.players.useCases.getPlayerUseCase

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.domain.repos.PlayerRepository
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/21 13:44
 *   @description:
 */
@Service
class GetPlayerUseCase(private val playerRepository: PlayerRepository) : UseCase<GetPlayerCommand, Player> {
    override fun execute(request: GetPlayerCommand): Result<Player> {
        if (request.playerNameOrUuid.isBlank()) {
            return GetPlayerErrors.GetPlayerError()
        }
        if (request.playerNameOrUuid.length >= 36) {
            val player = playerRepository.findByUuid(request.playerNameOrUuid)
                ?: return GetPlayerErrors.GetPlayerError()
            return Result.success(player)
        } else {
            val player = playerRepository.findByName(request.playerNameOrUuid)
                ?: return GetPlayerErrors.GetPlayerError()
            return Result.success(player)
        }
    }
}