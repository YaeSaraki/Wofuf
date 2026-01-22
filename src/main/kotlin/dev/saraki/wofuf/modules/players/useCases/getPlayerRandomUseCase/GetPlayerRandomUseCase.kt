package dev.saraki.wofuf.modules.players.useCases.getPlayerRandomUseCase

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.domain.repos.PlayerRepository
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.core.Result
import org.springframework.stereotype.Service

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/20 20:39
 *   @description:
 */
@Service
class GetPlayerRandomUseCase(private val playerRepository: PlayerRepository) : UseCase<Int, List<Player>> {
    override fun execute(request: Int): Result<List<Player>> {
        try {
            val players = playerRepository.findRandom(request)
            return Result.success(players)
        } catch (e: Exception) {
            return GetPlayerRandomErrors.GetRandomPlayerError()
        }
    }
}