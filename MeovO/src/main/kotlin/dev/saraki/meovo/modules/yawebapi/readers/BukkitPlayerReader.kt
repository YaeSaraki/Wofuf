package dev.saraki.meovo.modules.yawebapi.readers

import dev.saraki.meovo.modules.yawebapi.domain.PlayerQuery
import dev.saraki.meovo.modules.yawebapi.domain.PlayerResult
import dev.saraki.meovo.modules.yawebapi.domain.reader.PlayerReader
import org.bukkit.Bukkit
import org.bukkit.Statistic

class BukkitPlayerReader : PlayerReader {

    override fun read(
        playerName: String,
        query: PlayerQuery
    ): PlayerResult? {
        val online = Bukkit.getPlayer(playerName)

        if (online != null) {
            val loc = online.location
            return PlayerResult(
                uuid = online.uniqueId,
                name = online.name,
                online = true,
                world = loc.world?.name ?: "unknown",
                firstLogin = online.firstPlayed,
                lastLogin = online.lastPlayed,
                totalPlaytimeSeconds = online.getStatistic(Statistic.PLAY_ONE_MINUTE).toLong(),
                x = loc.x,
                y = loc.y,
                z = loc.z
            )
        }

        if (!query.includeOffline) return null

        val offline = Bukkit.getOfflinePlayer(playerName)
        if (!offline.hasPlayedBefore()) return null

        return PlayerResult(
            uuid = offline.uniqueId,
            name = offline.name ?: "unknown",
            online = false,
            world = "offline",
            firstLogin = offline.firstPlayed,
            lastLogin = offline.lastPlayed,
            totalPlaytimeSeconds = offline.getStatistic(Statistic.PLAY_ONE_MINUTE).toLong(),
        )
    }
}