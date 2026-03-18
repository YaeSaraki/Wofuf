package dev.saraki.meovo.modules.yawebapi.domain.reader

import dev.saraki.meovo.modules.yawebapi.domain.SkinQuery
import dev.saraki.meovo.modules.yawebapi.domain.SkinResult
import java.util.UUID

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/2/2 11:50
 *   @description:
 */
interface SkinReader {
    fun read(playerId: UUID, query: SkinQuery): SkinResult?
}