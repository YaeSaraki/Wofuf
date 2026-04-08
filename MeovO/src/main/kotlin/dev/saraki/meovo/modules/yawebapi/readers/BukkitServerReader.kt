package dev.saraki.meovo.modules.yawebapi.readers

import dev.saraki.meovo.modules.yawebapi.domain.ServerStatusResult
import dev.saraki.meovo.modules.yawebapi.domain.reader.ServerReader
import org.bukkit.Bukkit
import taboolib.common.platform.function.info

class BukkitServerReader : ServerReader {

    override fun readStatus(): ServerStatusResult {
        info("[BukkitServerReader] Reading server status...")
        
        try {
            val server = Bukkit.getServer()
            info("[BukkitServerReader] Server instance: ${server.javaClass.name}")
            info("[BukkitServerReader] Server name: ${server.name}")
            info("[BukkitServerReader] Online players: ${server.onlinePlayers.size}")
            info("[BukkitServerReader] Max players: ${server.maxPlayers}")
            
            val tps = getTPS()
            info("[BukkitServerReader] TPS: $tps")

            val result = ServerStatusResult(
                onlinePlayers = server.onlinePlayers.size,
                maxPlayers = server.maxPlayers,
                tps = tps,
                heartbeatStatus = true,
                updateTime = System.currentTimeMillis()
            )
            
            info("[BukkitServerReader] Status result created: $result")
            return result
        } catch (e: Exception) {
            info("[BukkitServerReader] ERROR reading status: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }

    /**
     * Get server TPS (Ticks Per Second) using reflection
     * Tries to get the actual TPS from CraftServer
     * Falls back to 20.0 (ideal TPS) if reflection fails
     */
    private fun getTPS(): Double {
        return try {
            val server = Bukkit.getServer()
            val craftServer = server.javaClass
            
            info("[BukkitServerReader] Attempting to get TPS via reflection from ${craftServer.name}")
            
            // Try to get TPS array from CraftServer
            val method = craftServer.getDeclaredMethod("getTPS")
            method.isAccessible = true
            val tpsArray = method.invoke(server) as? DoubleArray
            
            val tps = tpsArray?.firstOrNull() ?: 20.0
            info("[BukkitServerReader] Successfully retrieved TPS: $tps (array: ${tpsArray?.toList()})")
            tps
        } catch (e: Exception) {
            info("[BukkitServerReader] Failed to get TPS: ${e.javaClass.simpleName} - ${e.message}")
            // If reflection fails, return ideal TPS
            20.0
        }
    }
}