package dev.saraki.wofuf.modules.players.infra.repos.jpa.mappers.player

import dev.saraki.wofuf.modules.players.domain.valueObjects.PlayerSkin
import dev.saraki.wofuf.modules.players.infra.repos.jpa.entities.PlayerSkinEntity

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/2 15:56
 *   @description:
 */
class PlayerSkinMapper {
    companion object {
        fun toDomain(entity: PlayerSkinEntity?): PlayerSkin =
            PlayerSkin.Companion.create(
                type = entity?.type ?: "",
                skin = entity?.skin ?: "",
                cape = entity?.cape ?: ""
            ).getOrThrow()

        fun toEntity(skin: PlayerSkin?): PlayerSkinEntity {
            return PlayerSkinEntity(
                type = skin?.type ?: "",
                skin = skin?.skin ?: "",
                cape = skin?.cape ?: ""
            )
        }
    }
}