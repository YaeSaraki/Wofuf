package dev.saraki.meovo.modules.yawebapi.readers

import dev.saraki.meovo.modules.yawebapi.domain.SkinQuery
import dev.saraki.meovo.modules.yawebapi.domain.SkinResult
import dev.saraki.meovo.modules.yawebapi.domain.reader.SkinReader
import net.skinsrestorer.api.SkinsRestorerProvider
import net.skinsrestorer.api.storage.PlayerStorage
import org.bukkit.Bukkit
import taboolib.common.platform.function.warning
import java.util.UUID

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/2 11:51
 *   @description: 优先从官方读取皮肤/披风，降级使用SkinsRestorer插件获取皮肤
 */

class BukkitSkinReader : SkinReader {
    override fun read(playerId: UUID, query: SkinQuery): SkinResult? {
        val player = Bukkit.getOfflinePlayer(playerId)
        val playerName = player.name ?: "UNKNOWN_PLAYER"

        // 第一步：优先从Minecraft官方（GameProfile）获取皮肤和披风
        try {
            // 1. 校验playerProfile和textures非空，避免空指针
            val playerProfile = player.playerProfile
            val textures = playerProfile.textures

            // 2. 处理皮肤数据（非空判断）
            val skinTexture = textures.skin
            val skinUrl = skinTexture
            val skinModel = textures.skinModel.name

            // 3. 处理披风数据（非空判断，无披风则为UNKNOWN）
            val capeUrl = textures.cape?.toString() ?: ""

            // 4. 封装并返回官方渠道的结果
            if (skinUrl != null) {
                return SkinResult(
                    name = playerName,
                    uuid = player.uniqueId,
                    type = skinModel,
                    skin = skinUrl.toString(),
                    cape = capeUrl
                )
            }
        } catch (e: Exception) {
            // 捕获所有官方渠道的异常，打印详细日志（便于排查问题）
            warning("Failed to read skin/cape from Minecraft official: ${e.message}", e)
        }

        // 第二步：降级使用SkinsRestorer插件获取皮肤数据
        try {
            // 1. 获取SkinsRestorer API和玩家存储服务
            val skinsRestorerAPI = SkinsRestorerProvider.get()
            val playerStorage: PlayerStorage = skinsRestorerAPI.playerStorage

            // 2. 从插件中获取玩家皮肤属性（返回Optional<Property>）
            val skinProperty = playerStorage.getSkinForPlayer(playerId, playerName)

            // 3. 处理插件返回的皮肤数据（非空判断）
            if (skinProperty.isPresent) {
                val propertyValue = skinProperty.get().value
                return SkinResult(
                    name = playerName,
                    uuid = player.uniqueId,
                    type = "SKIN_RESTORER", // 标记数据来源为插件，更易区分
                    skin = propertyValue,
                    cape = "" // 插件渠道暂不处理披风，标记为未知
                )
            } else {
                warning("SkinsRestorer has no skin data for player: $playerName (UUID: $playerId)")
            }
        } catch (e: Exception) {
            // 捕获SkinsRestorer相关异常（如插件未安装、API版本不兼容等）
            warning("Failed to read skin from SkinsRestorer: ${e.message}", e)
        }

        // 第三步：所有渠道均失败，返回null
        warning("All skin read channels failed for player: $playerName (UUID: $playerId)")
        return null
    }
}