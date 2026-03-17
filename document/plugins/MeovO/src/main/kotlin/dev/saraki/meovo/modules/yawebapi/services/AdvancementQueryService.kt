package dev.saraki.meovo.modules.yawebapi.services

import dev.saraki.meovo.modules.yawebapi.domain.AdvancementResult
import dev.saraki.meovo.modules.yawebapi.domain.AdvancementQuery
import dev.saraki.meovo.modules.yawebapi.domain.reader.AdvancementReader
import dev.saraki.meovo.modules.yawebapi.readers.BukkitAdvancementReader
import java.util.UUID

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 11:42
 *   @description:
 */
object AdvancementQueryService {

    private val reader: AdvancementReader = BukkitAdvancementReader()

    fun query(players: List<UUID>, query: AdvancementQuery): List<AdvancementResult> {
        return players.mapNotNull { playerId ->
            reader.read(playerId, query)
        }
    }

    fun queryAll(query: AdvancementQuery): List<AdvancementResult> {
        return reader.readAll(query)
    }
}