package dev.saraki.wofuf.modules.players.useCases.getPlayerSkinUseCase

import dev.saraki.wofuf.modules.players.domain.Player
import dev.saraki.wofuf.modules.players.domain.repos.PlayerRepository
import dev.saraki.wofuf.modules.players.useCases.getPlayerUseCase.GetPlayerCommand
import dev.saraki.wofuf.modules.players.useCases.getPlayerUseCase.GetPlayerErrors
import dev.saraki.wofuf.shared.core.Result
import dev.saraki.wofuf.shared.core.UseCase
import org.springframework.stereotype.Service

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/2 17:39
 *   @description:
 */
@Service
class GetPlayerSkinUseCase(private val playerRepository: PlayerRepository) : UseCase<GetPlayerSkinCommand, GetPlayerSkinView> {
    override fun execute(request: GetPlayerSkinCommand): Result<GetPlayerSkinView> {
        val player = playerRepository.findByUuid(request.playerUuid)
            ?: return GetPlayerSkinErrors.GetPlayerSkinError()
        if (player.playerSkin.skin == null || player.playerSkin.type == null || player.playerSkin.cape == null) {
            return GetPlayerSkinErrors.GetPlayerSkinError()
        }
        return Result.success(GetPlayerSkinView(
            type = player.playerSkin.type,
            skin = player.playerSkin.skin,
            cape = player.playerSkin.cape,
        ))
    }
}