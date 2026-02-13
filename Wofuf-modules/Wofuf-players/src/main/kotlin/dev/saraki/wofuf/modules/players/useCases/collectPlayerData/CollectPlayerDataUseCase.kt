package dev.saraki.wofuf.modules.players.useCases.collectPlayerData

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerId
import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerName
import dev.saraki.wofuf.modules.players.domain.PlayerProps
import dev.saraki.wofuf.modules.players.infra.repos.PlayerRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

@Service
class CollectPlayerDataUseCase(
    private val playerRepo: PlayerRepo
) : UseCase<CollectPlayerDataCommand, Player> {
    override fun execute(request: CollectPlayerDataCommand): Result<Player> {

        val playerIdOrError = PlayerId.create(UniqueEntityId(request.uuid))
        val playerNameOrError = PlayerName.create(request.name)

        if (playerIdOrError.isFailure) {
            return Result.failure(playerIdOrError.exceptionOrThrow())
        }

        if (playerNameOrError.isFailure) {
            return Result.failure(playerNameOrError.exceptionOrThrow())
        }

        val playerId = playerIdOrError.getOrThrow()
        val playerName = playerNameOrError.getOrThrow()

        val player = playerRepo.findByPlayerId(playerId)

        // 如果玩家不存在，则创建新玩家
        if (player == null) {
            val newPlayerOrError = Player.create(
                props = PlayerProps(
                    playerName = playerName,
                    firstLogin = request.firstLogin,
                    lastLogin = request.lastLogin,
                    totalPlaytimeSeconds = request.totalPlaytimeSeconds,
                    updateTime = System.currentTimeMillis(),
                    statistics = request.statistics,
                    advancements = request.advancements,
                    playerSkin = request.playerSkin
                ),
                id = UniqueEntityId(request.uuid)
            )
            if (newPlayerOrError.isFailure) {
                return CollectPlayerErrors.CreatePlayerError(request.uuid)
            }
            val newPlayer = newPlayerOrError.getOrThrow()
            playerRepo.save(newPlayer)
            return Result.success(newPlayer)
        }

        val playerProps = PlayerProps(
            playerName = playerName,
            request.firstLogin,
            request.lastLogin,
            request.totalPlaytimeSeconds,
            System.currentTimeMillis(),
            request.statistics,
            request.advancements,
            request.playerSkin
        )

        // 更新玩家数据
        val updatePlayerOrError = player.updateProps(playerProps)

        if (updatePlayerOrError.isFailure) {
            return CollectPlayerErrors.UpdatePlayerError(request.uuid)
        }
        val updatedPlayer = updatePlayerOrError.getOrThrow()
        playerRepo.save(updatedPlayer)
        return Result.success(updatedPlayer)
    }
}
