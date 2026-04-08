package dev.saraki.wofuf.modules.players.scheduler

import dev.saraki.wofuf.modules.players.infra.client.MeovoClient
import dev.saraki.wofuf.modules.players.services.cache.ServerStatusCache
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 *   @author YaeSaraki
 *   @email ikaraswork@iCloud.com
 *   @date 2026/4/8
 *   @description: Scheduled task to periodically refresh server status
 */
@Component
class ServerStatusScheduler(
    private val meovoClient: MeovoClient,
    private val cache: ServerStatusCache
) {
    /**
     * Refresh server status every 30 seconds
     * Fetches latest status from Meovo plugin and updates cache
     */
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    fun refreshServerStatus() {
        try {
            val status = meovoClient.getServerStatus()
            if (status != null) {
                cache.put(status)
            }
        } catch (e: Exception) {
            // Log error but don't interrupt the scheduler
            // In production, should use proper logging
            e.printStackTrace()
        }
    }
}
