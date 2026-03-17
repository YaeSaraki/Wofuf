package dev.saraki.meovo.modules.yawebapi.readers

import dev.saraki.meovo.modules.yawebapi.domain.StatisticItem
import dev.saraki.meovo.modules.yawebapi.domain.StatisticQuery
import dev.saraki.meovo.modules.yawebapi.domain.StatisticResult
import dev.saraki.meovo.modules.yawebapi.domain.reader.StatisticReader
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Statistic
import org.bukkit.entity.EntityType
import java.util.UUID

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/1/19 11:25
 *   @description:
 */
class BukkitStatisticReader : StatisticReader {
    override fun read(playerId: UUID, query: StatisticQuery): StatisticResult? {
        val includeOffline = query.includeOffline
        val player =
            Bukkit.getPlayer(playerId) ?: if (includeOffline) Bukkit.getOfflinePlayer(playerId) else return null

        val statistics = mutableMapOf<String, StatisticItem>()
        try {
            Statistic.entries.forEach { stat ->
                if (query.category != null && !stat.type.name.contains(query.category, true)) return@forEach

                var v: Map<String, Int>? = null
                when (stat.type) {
                    Statistic.Type.UNTYPED ->
                        v = mapOf(stat.name to player.getStatistic(stat)).filter{
                            query.key == null || it.key.equals(query.key, true)
                        }

                    Statistic.Type.ITEM ->
                        v = Material.entries.asSequence()
                            .filter { it.isItem }
                            .filter { query.key == null || it.name.equals(query.key, true) }
                            .associate { it.name to player.getStatistic(stat, it) }

                    Statistic.Type.BLOCK ->
                        v = Material.entries.asSequence()
                            .filter { it.isBlock }
                            .filter { query.key == null || it.name.equals(query.key, true) }
                            .associate { it.name to player.getStatistic(stat, it) }

                    Statistic.Type.ENTITY ->
                        v = EntityType.entries.asSequence()
                            .filter { it.name != "UNKNOWN" }
                            .filter { query.key == null || it.name.equals(query.key, true) }
                            .associate { it.name to player.getStatistic(stat, it) }
                }

                v.forEach { t, u ->
                    statistics[t] = StatisticItem(
                        category = stat.type.name,
                        key = t,
                        value = u
                    )
                }
            }

            return StatisticResult(
                uuid = player.uniqueId,
                name = player.name,
                statistics = statistics
            )
        } catch (e: Exception) {
            error("Error while reading statistic for player ${player.name} (${player.uniqueId}): ${e.message}")
        }
    }

    override fun readAll(
        query: StatisticQuery
    ): List<StatisticResult> {
        val players = Bukkit.getOnlinePlayers().map { it.uniqueId }
        return players.mapNotNull { read(it, query) }
    }
}