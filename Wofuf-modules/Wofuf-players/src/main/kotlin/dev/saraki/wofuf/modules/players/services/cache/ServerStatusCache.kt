package dev.saraki.wofuf.modules.players.services.cache

import dev.saraki.wofuf.modules.players.domain.ServerStatus

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/4/8
 *   @description: Cache interface for server status
 */
interface ServerStatusCache {
    /**
     * Get cached server status
     * @return ServerStatus if cached, null otherwise
     */
    fun get(): ServerStatus?

    /**
     * Put server status into cache
     * @param status Server status to cache
     */
    fun put(status: ServerStatus)

    /**
     * Clear the cached server status
     */
    fun clear()
}
