package dev.saraki.meovo.modules.yawebapi.services

import dev.saraki.meovo.modules.yawebapi.domain.SkinQuery
import dev.saraki.meovo.modules.yawebapi.domain.reader.SkinReader
import dev.saraki.meovo.modules.yawebapi.domain.SkinResult
import dev.saraki.meovo.modules.yawebapi.readers.BukkitSkinReader
import java.util.UUID

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/2 11:50
 *   @description:
 */
object SkinQueryService {
    private val reader: SkinReader = BukkitSkinReader()
    /**
     * 查询单个玩家的皮肤
     */
    fun querySingle(
        playerUuid: UUID,
        query: SkinQuery
    ): SkinResult? {
        return reader.read(playerUuid, query)
    }
}