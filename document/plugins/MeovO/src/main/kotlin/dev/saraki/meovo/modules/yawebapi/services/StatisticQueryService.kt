package dev.saraki.meovo.modules.yawebapi.services

import dev.saraki.meovo.modules.yawebapi.domain.StatisticQuery
import dev.saraki.meovo.modules.yawebapi.domain.reader.StatisticReader
import dev.saraki.meovo.modules.yawebapi.readers.BukkitStatisticReader
import dev.saraki.meovo.modules.yawebapi.domain.StatisticResult
import java.util.UUID

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 11:21
 *   @description:
 */

object StatisticQueryService {

    private val reader: StatisticReader = BukkitStatisticReader()

    fun query(players: List<UUID>, query: StatisticQuery): List<StatisticResult> {
        return players.mapNotNull { playerId ->
            reader.read(playerId, query)
        }
    }

    fun queryAll(query: StatisticQuery): List<StatisticResult> {
        return reader.readAll(query)
    }
}