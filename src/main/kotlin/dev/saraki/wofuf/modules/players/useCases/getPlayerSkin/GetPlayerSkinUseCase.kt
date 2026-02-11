package dev.saraki.wofuf.modules.players.useCases.getPlayerSkin

import dev.saraki.wofuf.modules.players.domain.PlayerId
import dev.saraki.wofuf.modules.players.infra.repos.PlayerRepo
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import dev.saraki.wofuf.shared.domain.UniqueEntityId
import org.springframework.stereotype.Service

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/2 17:39
 *   @description:
 */
@Service
class GetPlayerSkinUseCase(private val playerRepository: PlayerRepo) :
    UseCase<GetPlayerSkinDto.Request, GetPlayerSkinDto.Response> {
    override fun execute(request: GetPlayerSkinDto.Request): Result<GetPlayerSkinDto.Response> {

        val playerIdOrError = PlayerId.create(UniqueEntityId(request.playerUuid))
        if (playerIdOrError.isFailure) {
            return Result.failure(playerIdOrError.exceptionOrThrow())
        }
        val playerId = playerIdOrError.getOrThrow()

        val player = playerRepository.findByPlayerId(playerId)
            ?: return GetPlayerSkinErrors.GetPlayerSkinError()

        return Result.success(
            GetPlayerSkinDto.Response(
                type = player.playerSkin.type,
                skin = player.playerSkin.skin,
                cape = player.playerSkin.cape,
            )
        )
    }
}