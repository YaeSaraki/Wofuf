package dev.saraki.meovo.modules.yawebapi.services

import dev.saraki.meovo.modules.yawebapi.domain.PlayerQuery
import dev.saraki.meovo.modules.yawebapi.domain.PlayerResult
import dev.saraki.meovo.modules.yawebapi.domain.reader.PlayerReader
import dev.saraki.meovo.modules.yawebapi.readers.BukkitPlayerReader
import org.bukkit.Bukkit

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 11:51
 *   @description:
 */

object PlayerQueryService {

    private val reader: PlayerReader = BukkitPlayerReader()

    /**
     * 查询单个玩家（在线 / 离线）
     */
    fun querySingle(
        playerName: String,
        query: PlayerQuery
    ): PlayerResult? {
        return reader.read(playerName, query)
    }

    /**
     * 查询所有在线玩家
     */
    fun queryAllOnline(): List<PlayerResult> {
        return Bukkit.getOnlinePlayers()
            .mapNotNull { player ->
                reader.read(player.name, PlayerQuery())
            }
    }
}