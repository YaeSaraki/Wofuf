package dev.saraki.wofuf.modules.players.useCases.collectPlayerDataUseCase

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.domain.PlayerId
import dev.saraki.wofuf.modules.players.domain.PlayerProps
import dev.saraki.wofuf.modules.players.domain.repos.PlayerRepository
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service

@Service
class CollectPlayerDataUseCase(
    private val playerRepo: PlayerRepository
) : UseCase<CollectPlayerDataCommand, Player> {
    override fun execute(request: CollectPlayerDataCommand): Result<Player> {
        val player = playerRepo.findByUuid(request.uuid)

        // 如果玩家不存在，则创建新玩家
        if (player == null) {
            val newPlayerOrError = Player.create(
                id = PlayerId.create(request.uuid).getOrThrow(),
                props = PlayerProps(
                    name = request.name,
                    firstLogin = request.firstLogin,
                    lastLogin = request.lastLogin,
                    totalPlaytimeSeconds = request.totalPlaytimeSeconds,
                    updateTime = System.currentTimeMillis()
                ),
                statistics = request.statistics,
                advancements = request.advancements
            ).getOrNull()

            if (newPlayerOrError == null) {
                return CollectPlayerErrors.CreatePlayerError(request.uuid)
            }

            playerRepo.save(newPlayerOrError)
            return Result.success(newPlayerOrError)
        }

        // 更新玩家统计数据和进度数据
        player.updateStatistics(request.statistics).getOrThrow()
            .updateAdvancements(request.advancements).getOrThrow()

        playerRepo.save(player)

        return Result.success(player)
    }
}
