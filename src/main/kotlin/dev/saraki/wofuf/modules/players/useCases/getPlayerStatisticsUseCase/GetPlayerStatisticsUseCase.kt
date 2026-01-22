package dev.saraki.wofuf.modules.players.useCases.getPlayerStatisticsUseCase

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
class GetPlayerStatisticsUseCase(private val playerRepository: PlayerRepository) : UseCase<GetPlayerStatisticsCommand, Player> {
    override fun execute(request: GetPlayerStatisticsCommand): Result<Player> {
        val player = playerRepository.findByName(request.playerName)
            ?: return GetPlayerStatisticsErrors.GetPlayerError()
        return Result.success(player)
    }
}