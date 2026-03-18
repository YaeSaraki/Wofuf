package dev.saraki.meovo.modules.yawebapi.domain.reader

import dev.saraki.meovo.modules.yawebapi.domain.AdvancementQuery
import dev.saraki.meovo.modules.yawebapi.domain.AdvancementResult
import java.util.UUID

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 11:42
 *   @description:
 */
interface AdvancementReader {
    fun read(playerId: UUID, query: AdvancementQuery): AdvancementResult?
    fun readAll(query: AdvancementQuery): List<AdvancementResult>
}