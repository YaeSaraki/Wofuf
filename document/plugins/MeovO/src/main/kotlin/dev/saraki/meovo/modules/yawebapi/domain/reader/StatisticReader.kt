package dev.saraki.meovo.modules.yawebapi.domain.reader

import dev.saraki.meovo.modules.yawebapi.domain.StatisticQuery
import dev.saraki.meovo.modules.yawebapi.domain.StatisticResult
import java.util.UUID

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 11:23
 *   @description:
 */
interface StatisticReader {
    fun read(playerId: UUID, query: StatisticQuery): StatisticResult?
    fun readAll(query: StatisticQuery): List<StatisticResult>
}