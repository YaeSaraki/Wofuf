package dev.saraki.wofuf.modules.players.useCases.getPlayerAdvancementsUseCase

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
class GetPlayerAdvancementsUseCase(private val playerRepository: PlayerRepository) : UseCase<GetPlayerAdvancementsCommand, Player> {
    override fun execute(request: GetPlayerAdvancementsCommand): Result<Player> {
        val player = playerRepository.findByName(request.playerName)
            ?: return GetPlayerAdvancementsErrors.GetPlayerError()
        return Result.success(player)
    }
}